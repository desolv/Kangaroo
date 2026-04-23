package gg.desolve.kangaroo.velocity.service;

import gg.desolve.kangaroo.rpc.RemoteBroadcast;
import gg.desolve.kangaroo.rpc.RemoteCommand;
import gg.desolve.kangaroo.util.JsonUtil;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

public class RpcService {

    public void executeOnServer(String serverId, String command) {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        plugin.getRedisStorage().publish(
                "kangaroo:rpc:" + serverId,
                JsonUtil.GSON.toJson(new RemoteCommand(plugin.getProxyId(), command)));
    }

    public void broadcastOnServer(String serverId, String message) {
        broadcastOnServer(serverId, message, null);
    }

    public void broadcastOnServer(String serverId, String message, String permission) {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        plugin.getRedisStorage().publish(
                "kangaroo:broadcast:" + serverId,
                JsonUtil.GSON.toJson(new RemoteBroadcast(plugin.getProxyId(), message, permission)));
    }
}
