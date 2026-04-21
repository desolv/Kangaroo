package gg.desolve.kangaroo.server;

import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServerMonitor {

    private final ServerService serverService;
    private final RedisStorage redis;
    private final Consumer<ServerEvent> eventHandler;
    private final Map<String, ServerType> knownServers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor;
    private RedisStorage.Subscription subscription;

    public ServerMonitor(ServerService serverService, RedisStorage redis, Consumer<ServerEvent> eventHandler) {
        this.serverService = serverService;
        this.redis = redis;
        this.eventHandler = eventHandler;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "kangaroo-server-monitor");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        for (Server server : serverService.getAll()) {
            knownServers.put(server.getId(), server.getType());
        }

        this.subscription = redis.subscribe("kangaroo:server-events", (channel, message) -> {
            try {
                ServerEvent event = JsonUtil.GSON.fromJson(message, ServerEvent.class);
                switch (event.getType()) {
                    case CONNECTED -> {
                        if (knownServers.put(event.getServerId(), event.getServerType()) == null) {
                            eventHandler.accept(event);
                        }
                    }
                    case LOADED -> {
                        knownServers.put(event.getServerId(), event.getServerType());
                        eventHandler.accept(event);
                    }
                    case DISCONNECTED -> {
                        if (knownServers.remove(event.getServerId()) != null) {
                            eventHandler.accept(event);
                        }
                    }
                    case DIED -> {
                        // DIED is detected locally via polling; ignore pubsub echoes.
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executor.scheduleAtFixedRate(this::check, 5, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
        executor.shutdown();
    }

    private void check() {
        try {
            List<Server> current = serverService.getAll();
            Set<String> currentIds = current.stream()
                    .map(Server::getId)
                    .collect(Collectors.toSet());

            for (Server server : current) {
                knownServers.putIfAbsent(server.getId(), server.getType());
            }

            Set<String> died = new HashSet<>(knownServers.keySet());
            died.removeAll(currentIds);

            for (String serverId : died) {
                ServerType type = knownServers.remove(serverId);

                ServerEvent event = new ServerEvent(
                        ServerEventType.DIED,
                        serverId,
                        type,
                        0,
                        System.currentTimeMillis()
                );

                eventHandler.accept(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
