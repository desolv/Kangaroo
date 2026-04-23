package gg.desolve.kangaroo.velocity.scheduler;

import com.velocitypowered.api.proxy.ProxyServer;
import gg.desolve.kangaroo.scheduler.KangarooScheduler;

import java.util.concurrent.TimeUnit;

public class VelocityScheduler implements KangarooScheduler {

    private final ProxyServer proxyServer;
    private final Object plugin;

    public VelocityScheduler(ProxyServer proxyServer, Object plugin) {
        this.proxyServer = proxyServer;
        this.plugin = plugin;
    }

    @Override
    public ScheduledTask scheduleRepeating(Runnable task, long initialDelaySeconds, long periodSeconds) {
        com.velocitypowered.api.scheduler.ScheduledTask scheduled = proxyServer.getScheduler()
                .buildTask(plugin, task)
                .delay(initialDelaySeconds, TimeUnit.SECONDS)
                .repeat(periodSeconds, TimeUnit.SECONDS)
                .schedule();
        return scheduled::cancel;
    }
}
