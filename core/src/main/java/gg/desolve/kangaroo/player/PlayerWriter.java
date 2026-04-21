package gg.desolve.kangaroo.player;

import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;

import java.util.Map;
import java.util.UUID;

public class PlayerWriter {

    private final RedisStorage redis;

    public PlayerWriter(RedisStorage redis) {
        this.redis = redis;
    }

    public void addPlayer(KangarooPlayer player) {
        redis.execute(jedis -> {
            jedis.hset("kangaroo:players", player.getUuid().toString(), JsonUtil.GSON.toJson(player));
            jedis.hset("kangaroo:playernames", player.getName().toLowerCase(), player.getUuid().toString());
            jedis.publish("kangaroo:events", JsonUtil.GSON.toJson(PlayerEvent.login(player)));
        });
    }

    public void removePlayer(UUID uuid, String name, String proxy) {
        redis.execute(jedis -> {
            jedis.hdel("kangaroo:players", uuid.toString());
            jedis.hdel("kangaroo:playernames", name.toLowerCase());
            jedis.publish("kangaroo:events", JsonUtil.GSON.toJson(PlayerEvent.logout(uuid, name, proxy)));
        });
    }

    public void updateServer(UUID uuid, String name, String proxy, String newServer, String lastServer, long switchTime) {
        redis.execute(jedis -> {
            String json = jedis.hget("kangaroo:players", uuid.toString());
            if (json == null) return;

            KangarooPlayer player = JsonUtil.GSON.fromJson(json, KangarooPlayer.class);
            player.setServer(newServer);
            player.setLastServer(lastServer);
            player.setLastSwitchTime(switchTime);
            player.setLastHeartbeat(System.currentTimeMillis());

            jedis.hset("kangaroo:players", uuid.toString(), JsonUtil.GSON.toJson(player));
            jedis.publish("kangaroo:events", JsonUtil.GSON.toJson(
                    PlayerEvent.switchServer(uuid, name, proxy, newServer, lastServer, switchTime)));
        });
    }

    public void updateHeartbeat(UUID uuid) {
        redis.execute(jedis -> {
            String json = jedis.hget("kangaroo:players", uuid.toString());
            if (json == null) return;

            KangarooPlayer player = JsonUtil.GSON.fromJson(json, KangarooPlayer.class);
            player.setLastHeartbeat(System.currentTimeMillis());

            jedis.hset("kangaroo:players", uuid.toString(), JsonUtil.GSON.toJson(player));
        });
    }

    public void removeAllByProxy(String proxyId) {
        redis.execute(jedis -> {
            Map<String, String> entries = jedis.hgetAll("kangaroo:players");

            for (Map.Entry<String, String> entry : entries.entrySet()) {
                KangarooPlayer player = JsonUtil.GSON.fromJson(entry.getValue(), KangarooPlayer.class);
                if (proxyId.equals(player.getProxy())) {
                    jedis.hdel("kangaroo:players", entry.getKey());
                    jedis.hdel("kangaroo:playernames", player.getName().toLowerCase());
                    jedis.publish("kangaroo:events", JsonUtil.GSON.toJson(
                            PlayerEvent.logout(player.getUuid(), player.getName(), player.getProxy())));
                }
            }
        });
    }
}
