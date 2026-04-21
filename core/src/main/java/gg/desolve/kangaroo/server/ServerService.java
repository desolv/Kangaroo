package gg.desolve.kangaroo.server;

import gg.desolve.kangaroo.storage.RedisStorage;
import gg.desolve.kangaroo.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ServerService {

    private final RedisStorage redis;

    public ServerService(RedisStorage redis) {
        this.redis = redis;
    }

    public List<Server> getAll() {
        return redis.query(jedis -> {
            Set<String> keys = jedis.keys("kangaroo:servers:*");
            List<Server> servers = new ArrayList<>();

            for (String key : keys) {
                String json = jedis.get(key);
                if (json != null) {
                    servers.add(JsonUtil.GSON.fromJson(json, Server.class));
                }
            }

            return servers;
        });
    }

    public List<Server> getByType(ServerType type) {
        return getAll().stream()
                .filter(server -> server.getType() == type)
                .toList();
    }

    public List<Server> getServers() {
        return getByType(ServerType.SERVER);
    }

    public List<Server> getProxies() {
        return getByType(ServerType.PROXY);
    }

    public Optional<Server> getById(String id) {
        return getServers().stream()
                .filter(server -> server.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    public Optional<Server> getByAddress(String host, int port) {
        return getServers().stream()
                .filter(server -> server.getPort() == port && server.getHost().equalsIgnoreCase(host))
                .findFirst();
    }

    public Optional<Server> getSentinelProxy() {
        String sentinelId = redis.query(jedis -> jedis.get("kangaroo:sentinel"));
        if (sentinelId == null) return Optional.empty();
        return getProxies().stream()
                .filter(p -> p.getId().equals(sentinelId))
                .findFirst();
    }
}
