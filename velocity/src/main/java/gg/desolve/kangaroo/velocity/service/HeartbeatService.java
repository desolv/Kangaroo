package gg.desolve.kangaroo.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import gg.desolve.kangaroo.heartbeat.Heartbeat;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerType;
import gg.desolve.kangaroo.storage.RedisStorage;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;

import java.util.concurrent.TimeUnit;

public class HeartbeatService {

    private final Heartbeat heartbeat;
    private final Task task;

    public HeartbeatService(ProxyServer proxy, RedisStorage redisStorage, String serverId) {
        Server server = new Server(
                serverId,
                ServerType.PROXY,
                proxy.getPlayerCount(),
                proxy.getConfiguration().getShowMaxPlayers(),
                proxy.getBoundAddress().getHostString(),
                proxy.getBoundAddress().getPort(),
                proxy.getVersion().toString(),
                0.0,
                System.currentTimeMillis(),
                0
        );

        this.heartbeat = new Heartbeat(redisStorage, server);
        heartbeat.start();

        this.task = Schedulers.builder()
                .sync()
                .every(5, TimeUnit.SECONDS)
                .run(() -> {
                    heartbeat.getServer().setTotalPlayers(proxy.getPlayerCount());
                    heartbeat.getServer().setMaxPlayers(proxy.getConfiguration().getShowMaxPlayers());
                });
    }

    public void shutdown() {
        task.close();
        heartbeat.stop();
    }
}
