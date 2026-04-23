package gg.desolve.kangaroo.player;

import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerService {

    private final RedisStorage redis;

    public PlayerService(RedisStorage redis) {
        this.redis = redis;
    }

    public List<KangarooPlayer> getAll() {
        return redis.query(jedis -> {
            Map<String, String> entries = jedis.hgetAll("kangaroo:players");
            List<KangarooPlayer> players = new ArrayList<>();

            for (String json : entries.values()) {
                players.add(JsonUtil.GSON.fromJson(json, KangarooPlayer.class));
            }

            return players;
        });
    }

    public KangarooPlayer getByUuid(UUID uuid) {
        return redis.query(jedis -> {
            String json = jedis.hget("kangaroo:players", uuid.toString());
            return json != null ? JsonUtil.GSON.fromJson(json, KangarooPlayer.class) : null;
        });
    }

    public KangarooPlayer getByName(String name) {
        return redis.query(jedis -> {
            String uuid = jedis.hget("kangaroo:playernames", name.toLowerCase());
            if (uuid == null) return null;

            String json = jedis.hget("kangaroo:players", uuid);
            return json != null ? JsonUtil.GSON.fromJson(json, KangarooPlayer.class) : null;
        });
    }

    public List<KangarooPlayer> getByServer(String serverId) {
        return getAll().stream()
                .filter(player -> serverId.equals(player.getServer()))
                .toList();
    }

    public List<KangarooPlayer> getByProxy(String proxyId) {
        return getAll().stream()
                .filter(player -> proxyId.equals(player.getProxy()))
                .toList();
    }

    public int getTotalCount() {
        return redis.query(jedis -> jedis.hlen("kangaroo:players")).intValue();
    }

    public boolean isOnline(UUID uuid) {
        return redis.query(jedis -> jedis.hexists("kangaroo:players", uuid.toString()));
    }
}
