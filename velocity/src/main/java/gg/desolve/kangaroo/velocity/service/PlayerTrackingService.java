package gg.desolve.kangaroo.velocity.service;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerTrackingService {

    private final ScheduledExecutorService heartbeatExecutor;

    public PlayerTrackingService() {
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "kangaroo-player-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        plugin.getServer().getEventManager().register(plugin, this);
        seedExistingPlayers();
        startHeartbeats();
    }

    public void stop() {
        heartbeatExecutor.shutdown();
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        plugin.getPlayerWriter().removeAllByProxy(plugin.getProxyId());
    }

    @Subscribe(priority = 1000)
    public void onPreLogin(PreLoginEvent event) {
        if (!event.getResult().isAllowed()) return;

        KangarooVelocity plugin = KangarooVelocity.getInstance();
        UUID uuid = event.getUniqueId();
        KangarooPlayer existing = plugin.getPlayerService().getByUuid(uuid);

        if (existing == null) return;

        if (existing.getProxy().equals(plugin.getProxyId())) {
            plugin.getPlayerWriter().removePlayer(uuid, existing.getName(), existing.getProxy());
            return;
        }

        Set<String> liveProxies = plugin.getServerService().getProxies().stream()
                .map(Server::getId)
                .collect(Collectors.toSet());

        if (liveProxies.contains(existing.getProxy())) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(
                    Component.text("Already connected from another proxy.")
            ));
        } else {
            plugin.getPlayerWriter().removePlayer(uuid, existing.getName(), existing.getProxy());
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        Player player = event.getPlayer();

        KangarooPlayer tracked = new KangarooPlayer(
                player.getUniqueId(),
                player.getUsername(),
                plugin.getProxyId(),
                null,
                System.currentTimeMillis(),
                null,
                0,
                System.currentTimeMillis()
        );

        plugin.getPlayerWriter().addPlayer(tracked);
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        Player player = event.getPlayer();
        String newServer = resolveKangarooId(event.getServer().getServerInfo().getAddress());
        String lastServer = event.getPreviousServer()
                .map(s -> resolveKangarooId(s.getServerInfo().getAddress()))
                .orElse(null);

        plugin.getPlayerWriter().updateServer(
                player.getUniqueId(),
                player.getUsername(),
                plugin.getProxyId(),
                newServer,
                lastServer,
                System.currentTimeMillis()
        );
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        Player player = event.getPlayer();
        plugin.getPlayerWriter().removePlayer(player.getUniqueId(), player.getUsername(), plugin.getProxyId());
    }

    private void seedExistingPlayers() {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        ProxyServer proxy = plugin.getServer();
        for (Player player : proxy.getAllPlayers()) {
            String server = player.getCurrentServer()
                    .map(s -> resolveKangarooId(s.getServerInfo().getAddress()))
                    .orElse(null);

            KangarooPlayer tracked = new KangarooPlayer(
                    player.getUniqueId(),
                    player.getUsername(),
                    plugin.getProxyId(),
                    server,
                    System.currentTimeMillis(),
                    null,
                    0,
                    System.currentTimeMillis()
            );

            plugin.getPlayerWriter().addPlayer(tracked);
        }
    }

    private String resolveKangarooId(InetSocketAddress address) {
        if (address == null) return null;
        return KangarooVelocity.getInstance().getServerService()
                .getByAddress(address.getHostString(), address.getPort())
                .map(Server::getId)
                .orElse(null);
    }

    private void startHeartbeats() {
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            try {
                ProxyServer proxy = KangarooVelocity.getInstance().getServer();
                for (Player player : proxy.getAllPlayers()) {
                    KangarooVelocity.getInstance().getPlayerWriter().updateHeartbeat(player.getUniqueId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
}
