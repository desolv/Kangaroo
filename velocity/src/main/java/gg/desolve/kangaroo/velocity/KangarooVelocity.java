package gg.desolve.kangaroo.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import gg.desolve.kangaroo.storage.ConfigStorage;
import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.velocity.service.CommandService;
import gg.desolve.kangaroo.velocity.service.ConfigService;
import gg.desolve.kangaroo.velocity.service.HeartbeatService;
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
    private HeartbeatService heartbeatService;

    @Inject
    public KangarooVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.configService = new ConfigService(dataDirectory);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        logger.info("Initialising Kangaroo for velocity proxy...");

        ConfigStorage config = configService.load("config.yml");

        this.redisStorage = new RedisStorage(config.get("redis.uri"));
        this.heartbeatService = new HeartbeatService(
                server,
                redisStorage,
                config.get("heartbeat.server-id")
        );

        new CommandService(server, this);

        logger.info("Kangaroo has been enabled.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (heartbeatService != null) {
            heartbeatService.shutdown();
        }
        if (redisStorage != null) {
            redisStorage.close();
        }
    }
}
