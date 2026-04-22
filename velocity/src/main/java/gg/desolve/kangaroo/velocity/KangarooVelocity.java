package gg.desolve.kangaroo.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.desolve.kangaroo.player.PlayerCache;
import gg.desolve.kangaroo.player.PlayerEventSubscriber;
import gg.desolve.kangaroo.player.PlayerService;
import gg.desolve.kangaroo.player.PlayerWriter;
import gg.desolve.kangaroo.server.ServerMonitor;
import gg.desolve.kangaroo.server.ServerService;
import gg.desolve.kangaroo.storage.ConfigStorage;
import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.velocity.service.*;
import lombok.Getter;
import org.slf4j.Logger;

import java.nio.file.Path;

@Getter
@Plugin(
        id = "kangaroo",
        name = "Kangaroo",
        version = "1.0"
)
public final class KangarooVelocity {

    @Getter
    private static KangarooVelocity instance;
    private final ProxyServer server;
    private final Logger logger;

    private final ConfigService configService;
    private RedisStorage redisStorage;
    private ServerService serverService;
    private HeartbeatService heartbeatService;
    private ServerMonitor serverMonitor;
    private PlayerService playerService;
    private PlayerWriter playerWriter;
    private PlayerCache playerCache;
    private PlayerEventSubscriber playerEventSubscriber;
    private PlayerTrackingService playerTrackingService;
    private PlayerCleanupService playerCleanupService;
    private RpcService rpcService;
    private RedirectService redirectService;
    private SentinelService sentinelService;
    private String proxyId;
    private long loadStartTime;

    @Inject
    public KangarooVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.configService = new ConfigService(dataDirectory);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.loadStartTime = System.currentTimeMillis();
        instance = this;
        logger.info("Initialising Kangaroo for velocity proxy...");

        ConfigStorage config = configService.load("config.yml");
        this.proxyId = config.get("server.id");
        this.redisStorage = new RedisStorage(config.get("redis.uri"));
        this.serverService = new ServerService(redisStorage);
        this.playerService = new PlayerService(redisStorage);
        this.playerWriter = new PlayerWriter(redisStorage);
        this.playerCache = new PlayerCache(playerService);

        this.heartbeatService = new HeartbeatService(config.getStringList("server.groups"));

        this.serverMonitor = new ServerMonitor(serverService, redisStorage, serverEvent -> {
            String message = serverEvent.toMessage();
            server.getAllPlayers().stream()
                    .filter(p -> p.hasPermission("kangaroo.admin.notify"))
                    .forEach(p -> Message.send(p, message));
        });
        serverMonitor.start();

        playerCache.start();

        this.playerEventSubscriber = new PlayerEventSubscriber(redisStorage, playerCache);
        playerEventSubscriber.start();

        this.playerTrackingService = new PlayerTrackingService();
        playerTrackingService.start();

        this.playerCleanupService = new PlayerCleanupService();
        playerCleanupService.start();

        this.rpcService = new RpcService();

        this.redirectService = new RedirectService();
        redirectService.start();

        this.sentinelService = new SentinelService();
        sentinelService.start();

        new CommandService();

        heartbeatService.markLoaded();
        logger.info("Kangaroo has been enabled.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (sentinelService != null) {
            sentinelService.stop();
        }
        if (redirectService != null) {
            redirectService.stop();
        }
        if (playerCleanupService != null) {
            playerCleanupService.stop();
        }
        if (playerTrackingService != null) {
            playerTrackingService.stop();
        }
        if (playerEventSubscriber != null) {
            playerEventSubscriber.stop();
        }
        if (playerCache != null) {
            playerCache.stop();
        }
        if (serverMonitor != null) {
            serverMonitor.stop();
        }
        if (heartbeatService != null) {
            heartbeatService.shutdown();
        }
        if (redisStorage != null) {
            redisStorage.close();
        }
    }
}
