package gg.desolve.kangaroo.velocity.service;

import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerCleanupService {

    private final ScheduledExecutorService executor;

    public PlayerCleanupService() {
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "kangaroo-player-cleanup");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        executor.scheduleAtFixedRate(this::cleanup, 10, 10, TimeUnit.SECONDS);
    }

    public void stop() {
        executor.shutdown();
    }

    private void cleanup() {
        try {
            KangarooVelocity plugin = KangarooVelocity.getInstance();
            if (!plugin.getSentinelService().isSentinel()) return;

            Set<String> liveProxyIds = plugin.getServerService().getProxies().stream()
                    .map(Server::getId)
                    .collect(Collectors.toSet());

            for (KangarooPlayer player : plugin.getPlayerService().getAll()) {
                if (!liveProxyIds.contains(player.getProxy())) {
                    plugin.getPlayerWriter().removePlayer(player.getUuid(), player.getName(), player.getProxy());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
