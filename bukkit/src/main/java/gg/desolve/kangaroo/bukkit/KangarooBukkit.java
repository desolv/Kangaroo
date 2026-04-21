package gg.desolve.kangaroo.bukkit;

import gg.desolve.kangaroo.bukkit.service.CommandService;
import gg.desolve.kangaroo.bukkit.service.ConfigService;
import gg.desolve.kangaroo.bukkit.service.HeartbeatService;
import gg.desolve.kangaroo.bukkit.service.RpcService;
import gg.desolve.kangaroo.player.PlayerCache;
import gg.desolve.kangaroo.player.PlayerEventSubscriber;
import gg.desolve.kangaroo.player.PlayerService;
import gg.desolve.kangaroo.server.ServerService;
import gg.desolve.kangaroo.storage.ConfigStorage;
import gg.desolve.kangaroo.storage.RedisStorage;
import lombok.Getter;
import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.event.server.ServerLoadEvent;

@Getter
public class KangarooBukkit extends ExtendedJavaPlugin {

    @Getter
    private static KangarooBukkit instance;
    private ConfigService configService;
    private RedisStorage redisStorage;
    private HeartbeatService heartbeatService;
    private ServerService serverService;
    private PlayerService playerService;
    private PlayerCache playerCache;
    private PlayerEventSubscriber playerEventSubscriber;
    private RpcService rpcService;
    private String serverId;
    private long loadStartTime;

    @Override
    protected void enable() {
        this.loadStartTime = System.currentTimeMillis();
        instance = this;
        this.getLogger().info("Initialising Kangaroo for bukkit server...");

        configService = new ConfigService(this);
        ConfigStorage config = configService.load("config.yml");

        this.serverId = config.get("heartbeat.server-id");
        this.redisStorage = new RedisStorage(config.get("redis.uri"));
        this.serverService = new ServerService(redisStorage);
        this.playerService = new PlayerService(redisStorage);
        this.playerCache = new PlayerCache(playerService);

        this.heartbeatService = new HeartbeatService();

        playerCache.start();

        this.playerEventSubscriber = new PlayerEventSubscriber(redisStorage, playerCache);
        playerEventSubscriber.start();

        this.rpcService = new RpcService();
        rpcService.start();

        new CommandService();

        Events.subscribe(ServerLoadEvent.class)
                .handler(e -> heartbeatService.markLoaded())
                .bindWith(this);

        this.getLogger().info("Kangaroo has been enabled.");
    }

    @Override
    protected void disable() {
        if (rpcService != null) {
            rpcService.stop();
        }
        if (playerEventSubscriber != null) {
            playerEventSubscriber.stop();
        }
        if (playerCache != null) {
            playerCache.stop();
        }
        if (heartbeatService != null) {
            heartbeatService.shutdown();
        }
        if (redisStorage != null) {
            redisStorage.close();
        }
    }
}
