package gg.desolve.kangaroo.bukkit;

import gg.desolve.kangaroo.bukkit.service.ConfigService;
import gg.desolve.kangaroo.bukkit.service.HeartbeatService;
import gg.desolve.kangaroo.server.ServerService;
import gg.desolve.kangaroo.storage.ConfigStorage;
import gg.desolve.kangaroo.storage.RedisStorage;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class KangarooBukkit extends JavaPlugin {

    @Getter
    private static KangarooBukkit instance;
    private ConfigService configService;
    private RedisStorage redisStorage;
    private HeartbeatService heartbeatService;
    private ServerService serverService;

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("Initialising Kangaroo for bukkit server...");

        configService = new ConfigService(this);
        ConfigStorage config = configService.load("config.yml");

        this.redisStorage = new RedisStorage(config.get("redis.uri"));
        this.heartbeatService = new HeartbeatService(
                redisStorage,
                config.get("heartbeat.server-id")
        );
        serverService = new ServerService(redisStorage);

        this.getLogger().info("Kangaroo has been enabled.");
    }

    @Override
    public void onDisable() {
        if (heartbeatService != null) {
            heartbeatService.shutdown();
        }
        if (redisStorage != null) {
            redisStorage.close();
        }
    }
}
