package gg.desolve.kangaroo.velocity.service;

import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.scheduler.KangarooScheduler;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

import java.util.Set;
import java.util.stream.Collectors;

public class PlayerCleanupService {

    private KangarooScheduler.ScheduledTask task;

    public void start() {
        this.task = KangarooVelocity.getInstance().getScheduler().scheduleRepeating(this::cleanup, 10, 10);
    }

    public void stop() {
        if (task != null) task.cancel();
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
