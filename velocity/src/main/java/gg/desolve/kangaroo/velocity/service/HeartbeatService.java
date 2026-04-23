package gg.desolve.kangaroo.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import gg.desolve.kangaroo.heartbeat.Heartbeat;
import gg.desolve.kangaroo.scheduler.KangarooScheduler;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerType;
import gg.desolve.kangaroo.util.CpuUtil;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

import java.util.List;

public class HeartbeatService {

    private final Heartbeat heartbeat;
    private final KangarooScheduler.ScheduledTask statsTask;

    public HeartbeatService(List<String> groups) {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        ProxyServer proxy = plugin.getServer();

        Server server = new Server(
                plugin.getProxyId(),
                groups,
                ServerType.PROXY,
                proxy.getPlayerCount(),
                proxy.getConfiguration().getShowMaxPlayers(),
                proxy.getBoundAddress().getHostString(),
                proxy.getBoundAddress().getPort(),
                proxy.getVersion().getVersion(),
                0.0,
                0.0,
                System.currentTimeMillis(),
                0
        );

        this.heartbeat = new Heartbeat(
                plugin.getRedisStorage(),
                server,
                plugin.getLoadStartTime(),
                plugin.getScheduler()
        );
        heartbeat.start();

        this.statsTask = plugin.getScheduler().scheduleRepeating(() -> {
            heartbeat.getServer().setTotalPlayers(proxy.getPlayerCount());
            heartbeat.getServer().setMaxPlayers(proxy.getConfiguration().getShowMaxPlayers());
            heartbeat.getServer().setCpu(CpuUtil.processLoadPercent());
        }, 5, 5);
    }

    public void markLoaded() {
        heartbeat.markLoaded();
    }

    public void shutdown() {
        statsTask.cancel();
        heartbeat.stop();
    }
}
