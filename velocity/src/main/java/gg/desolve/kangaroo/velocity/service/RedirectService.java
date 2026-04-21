package gg.desolve.kangaroo.velocity.service;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.rpc.RedirectRequest;
import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

public class RedirectService {

    private RedisStorage.Subscription subscription;

    public void start() {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        this.subscription = plugin.getRedisStorage().subscribe(
                "kangaroo:redirect:" + plugin.getProxyId(),
                (channel, message) -> {
                    try {
                        RedirectRequest req = JsonUtil.GSON.fromJson(message, RedirectRequest.class);
                        redirectLocal(req.getPlayerId(), req.getTargetServer());
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

    public void redirect(KangarooPlayer player, String targetServer) {
        String myProxyId = KangarooVelocity.getInstance().getProxyId();
        if (myProxyId.equals(player.getProxy())) {
            redirectLocal(player.getUuid(), targetServer);
        } else {
            redirectRemote(player.getProxy(), player.getUuid(), targetServer);
        }
    }

    private boolean redirectLocal(UUID playerId, String serverId) {
        ProxyServer proxy = KangarooVelocity.getInstance().getServer();
        Optional<Player> player = proxy.getPlayer(playerId);
        if (player.isEmpty()) return false;

        Optional<RegisteredServer> server = resolveServer(serverId);
        if (server.isEmpty()) return false;

        player.get().createConnectionRequest(server.get()).fireAndForget();
        return true;
    }

    private void redirectRemote(String targetProxy, UUID playerId, String serverId) {
        KangarooVelocity.getInstance().getRedisStorage().publish(
                "kangaroo:redirect:" + targetProxy,
                JsonUtil.GSON.toJson(new RedirectRequest(playerId, serverId)));
    }

    private Optional<RegisteredServer> resolveServer(String serverId) {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        return plugin.getServerService().getById(serverId)
                .flatMap(target -> plugin.getServer().getAllServers().stream()
                        .filter(rs -> {
                            InetSocketAddress addr = rs.getServerInfo().getAddress();
                            return addr.getPort() == target.getPort()
                                    && addr.getHostString().equalsIgnoreCase(target.getHost());
                        })
                        .findFirst());
    }
}
