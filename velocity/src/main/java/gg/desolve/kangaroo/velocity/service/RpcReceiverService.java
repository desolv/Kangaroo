package gg.desolve.kangaroo.velocity.service;

import com.velocitypowered.api.proxy.ProxyServer;
import gg.desolve.kangaroo.rpc.RemoteCommand;
import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

public class RpcReceiverService {

    private RedisStorage.Subscription subscription;

    public void start() {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        this.subscription = plugin.getRedisStorage().subscribe(
                "kangaroo:rpc:" + plugin.getProxyId(),
                (channel, message) -> {
                    try {
                        RemoteCommand cmd = JsonUtil.GSON.fromJson(message, RemoteCommand.class);
                        ProxyServer proxy = plugin.getServer();
                        proxy.getCommandManager().executeAsync(proxy.getConsoleCommandSource(), cmd.getCommand());
                    } catch (Exception e) {
                        plugin.getLogger().error("Failed to dispatch RPC command on proxy", e);
                    }
                });
    }

    public void stop() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
