package gg.desolve.kangaroo.heartbeat;

import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerEvent;
import gg.desolve.kangaroo.server.ServerEventType;
import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;
import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Heartbeat {

    private final RedisStorage redis;
    @Getter
    private final Server server;
    private final long loadStartTime;
    private final ScheduledExecutorService executor;

    public Heartbeat(RedisStorage redis, Server server, long loadStartTime) {
        this.redis = redis;
        this.server = server;
        this.loadStartTime = loadStartTime;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "kangaroo-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        publish();
        publishServerEvent(ServerEventType.CONNECTED, 0);
        executor.scheduleAtFixedRate(
                this::publish,
                5,
                5,
                TimeUnit.SECONDS
        );
    }

    public void markLoaded() {
        publishServerEvent(ServerEventType.LOADED, System.currentTimeMillis() - loadStartTime);
    }

    public void stop() {
        executor.shutdown();
        try {
            publishServerEvent(ServerEventType.DISCONNECTED, 0);
            redis.execute(jedis -> jedis.del("kangaroo:servers:" + server.getId()));
        } catch (Exception ignored) {
        }
    }

    private void publish() {
        try {
            server.setLastHeartbeat(System.currentTimeMillis());
            redis.execute(jedis ->
                    jedis.setex("kangaroo:servers:" + server.getId(), 15, JsonUtil.GSON.toJson(server)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void publishServerEvent(ServerEventType type, long loadTimeMs) {
        try {
            ServerEvent event = new ServerEvent(
                    type,
                    server.getId(),
                    server.getType(),
                    loadTimeMs,
                    System.currentTimeMillis()
            );
            redis.publish("kangaroo:server-events", JsonUtil.GSON.toJson(event));
        } catch (Exception ignored) {
        }
    }
}
