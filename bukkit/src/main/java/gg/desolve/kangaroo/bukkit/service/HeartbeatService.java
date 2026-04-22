package gg.desolve.kangaroo.bukkit.service;

import gg.desolve.kangaroo.bukkit.KangarooBukkit;
import gg.desolve.kangaroo.heartbeat.Heartbeat;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerType;
import gg.desolve.kangaroo.util.CpuUtil;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class HeartbeatService {

    private final Heartbeat heartbeat;
    private final Task task;

    public HeartbeatService(List<String> groups) {
        KangarooBukkit plugin = KangarooBukkit.getInstance();

        Server server = new Server(
                plugin.getServerId(),
                groups,
                ServerType.SERVER,
                Bukkit.getOnlinePlayers().size(),
                Bukkit.getMaxPlayers(),
                Bukkit.getIp().isEmpty() ? "0.0.0.0" : Bukkit.getIp(),
                Bukkit.getPort(),
                Bukkit.getVersion(),
                20.0,
                0.0,
                System.currentTimeMillis(),
                0
        );

        this.heartbeat = new Heartbeat(plugin.getRedisStorage(), server, plugin.getLoadStartTime());
        heartbeat.start();

        this.task = Schedulers.builder()
                .sync()
                .every(5, TimeUnit.SECONDS)
                .run(() -> {
                    heartbeat.getServer().setTotalPlayers(Bukkit.getOnlinePlayers().size());
                    heartbeat.getServer().setMaxPlayers(Bukkit.getMaxPlayers());
                    heartbeat.getServer().setTps(Bukkit.getTPS()[0]);
                    heartbeat.getServer().setCpu(CpuUtil.processLoadPercent());
                });
    }

    public void markLoaded() {
        heartbeat.markLoaded();
    }

    public void shutdown() {
        task.close();
        heartbeat.stop();
    }
}
