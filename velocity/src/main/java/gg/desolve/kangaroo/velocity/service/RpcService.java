package gg.desolve.kangaroo.velocity.service;

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
}
