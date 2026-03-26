package gg.desolve.kangaroo.bukkit.service;

import gg.desolve.kangaroo.heartbeat.Heartbeat;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerType;
import gg.desolve.kangaroo.storage.RedisStorage;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class HeartbeatService {

    private final Heartbeat heartbeat;
    private final Task task;

    public HeartbeatService(RedisStorage redisStorage, String serverId) {
        Server server = new Server(
                serverId,
                ServerType.SERVER,
                Bukkit.getOnlinePlayers().size(),
                Bukkit.getMaxPlayers()
        );

        this.heartbeat = new Heartbeat(redisStorage, server);
        heartbeat.start();

        this.task = Schedulers.builder()
                .sync()
                .every(5, TimeUnit.SECONDS)
                .run(() -> {
                    heartbeat.getServer().setPlayerCount(Bukkit.getOnlinePlayers().size());
                    heartbeat.getServer().setMaxPlayers(Bukkit.getMaxPlayers());
                });
    }

    public void shutdown() {
        task.close();
        heartbeat.stop();
    }
}
