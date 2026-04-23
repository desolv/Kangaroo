package gg.desolve.kangaroo.bukkit.service;

import gg.desolve.kangaroo.bukkit.KangarooBukkit;
import gg.desolve.kangaroo.rpc.RemoteBroadcast;
import gg.desolve.kangaroo.rpc.RemoteCommand;
import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;
import gg.desolve.kangaroo.util.Message;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;

public class RpcService {

    private RedisStorage.Subscription commandSubscription;
    private RedisStorage.Subscription broadcastSubscription;

    public void start() {
        KangarooBukkit plugin = KangarooBukkit.getInstance();
        this.commandSubscription = plugin.getRedisStorage().subscribe(
                "kangaroo:rpc:" + plugin.getServerId(),
                (channel, message) -> {
                    try {
                        RemoteCommand command = JsonUtil.GSON.fromJson(message, RemoteCommand.class);
                        Schedulers.sync()
                                .run(() ->
                                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.getCommand()));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
        this.broadcastSubscription = plugin.getRedisStorage().subscribe(
                "kangaroo:broadcast:" + plugin.getServerId(),
                (channel, message) -> {
                    try {
                        RemoteBroadcast broadcast = JsonUtil.GSON.fromJson(message, RemoteBroadcast.class);
                        Schedulers.sync()
                                .run(() -> Message.broadcast(broadcast.getMessage()));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                });
    }

    public void stop() {
        if (commandSubscription != null) {
            commandSubscription.unsubscribe();
        }
        if (broadcastSubscription != null) {
            broadcastSubscription.unsubscribe();
        }
    }
}
