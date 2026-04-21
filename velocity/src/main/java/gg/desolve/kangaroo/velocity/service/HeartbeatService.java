package gg.desolve.kangaroo.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import gg.desolve.kangaroo.heartbeat.Heartbeat;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerType;
import gg.desolve.kangaroo.util.CpuUtil;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatService {

    private final Heartbeat heartbeat;
    private final ScheduledExecutorService executor;

    public HeartbeatService() {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        ProxyServer proxy = plugin.getServer();

        Server server = new Server(
                plugin.getProxyId(),
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

        this.heartbeat = new Heartbeat(plugin.getRedisStorage(), server, plugin.getLoadStartTime());
        heartbeat.start();

        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "kangaroo-heartbeat-stats");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(() -> {
            heartbeat.getServer().setTotalPlayers(proxy.getPlayerCount());
            heartbeat.getServer().setMaxPlayers(proxy.getConfiguration().getShowMaxPlayers());
            heartbeat.getServer().setCpu(CpuUtil.processLoadPercent());
        }, 5, 5, TimeUnit.SECONDS);
    }

    public void markLoaded() {
        heartbeat.markLoaded();
    }

    public void shutdown() {
        executor.shutdown();
        heartbeat.stop();
    }
}
