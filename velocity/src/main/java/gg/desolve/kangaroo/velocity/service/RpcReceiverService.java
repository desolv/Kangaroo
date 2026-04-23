package gg.desolve.kangaroo.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import gg.desolve.kangaroo.rpc.RemoteBroadcast;
import gg.desolve.kangaroo.rpc.RemoteCommand;
import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

public class RpcReceiverService {

    private RedisStorage.Subscription commandSubscription;
    private RedisStorage.Subscription broadcastSubscription;

    public void start() {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        this.commandSubscription = plugin.getRedisStorage().subscribe(
                "kangaroo:rpc:" + plugin.getProxyId(),
                (channel, message) -> {
                    try {
                        RemoteCommand command = JsonUtil.GSON.fromJson(message, RemoteCommand.class);
                        ProxyServer proxy = plugin.getServer();
                        proxy.getCommandManager().executeAsync(proxy.getConsoleCommandSource(), command.getCommand());
                    } catch (Exception exception) {
                        plugin.getLogger().error("Failed to dispatch RPC command on proxy", exception);
                    }
                });
        this.broadcastSubscription = plugin.getRedisStorage().subscribe(
                "kangaroo:broadcast:" + plugin.getProxyId(),
                (channel, message) -> {
                    try {
                        RemoteBroadcast broadcast = JsonUtil.GSON.fromJson(message, RemoteBroadcast.class);
                        plugin.getServer().getAllPlayers().forEach(player -> {
                            if (broadcast.getPermission() != null && !player.hasPermission(broadcast.getPermission()))
                                return;
                            Message.send(player, broadcast.getMessage());
                        });
                    } catch (Exception exception) {
                        plugin.getLogger().error("Failed to dispatch RPC broadcast on proxy", exception);
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
