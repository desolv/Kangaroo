package gg.desolve.kangaroo.bukkit.service;

import gg.desolve.kangaroo.bukkit.KangarooBukkit;
import gg.desolve.kangaroo.rpc.RemoteCommand;
import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;

public class RpcService {

    private RedisStorage.Subscription subscription;

    public void start() {
        KangarooBukkit plugin = KangarooBukkit.getInstance();
        this.subscription = plugin.getRedisStorage().subscribe(
                "kangaroo:rpc:" + plugin.getServerId(),
                (channel, message) -> {
                    try {
                        RemoteCommand cmd = JsonUtil.GSON.fromJson(message, RemoteCommand.class);
                        Schedulers.sync()
                                .run(() ->
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.getCommand()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public void stop() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
