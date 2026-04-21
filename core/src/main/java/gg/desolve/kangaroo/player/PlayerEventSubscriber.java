package gg.desolve.kangaroo.player;

import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;

public class PlayerEventSubscriber {

    private final RedisStorage redis;
    private final PlayerCache playerCache;
    private RedisStorage.Subscription subscription;

    public PlayerEventSubscriber(RedisStorage redis, PlayerCache playerCache) {
        this.redis = redis;
        this.playerCache = playerCache;
    }

    public void start() {
        this.subscription = redis.subscribe("kangaroo:events", (channel, message) -> {
            try {
                PlayerEvent event = JsonUtil.GSON.fromJson(message, PlayerEvent.class);
                switch (event.getType()) {
                    case LOGIN -> playerCache.handleLogin(new KangarooPlayer(
                            event.getUuid(),
                            event.getName(),
                            event.getProxy(),
                            event.getServer(),
                            event.getTimestamp(),
                            null,
                            0,
                            event.getTimestamp()
                    ));
                    case LOGOUT -> playerCache.handleLogout(event.getUuid());
                    case SWITCH -> playerCache.handleSwitch(
                            event.getUuid(),
                            event.getServer(),
                            event.getLastServer(),
                            event.getTimestamp()
                    );
                }
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
}
