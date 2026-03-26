package gg.desolve.kangaroo.server;

import com.google.gson.Gson;
import gg.desolve.kangaroo.storage.RedisStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ServerService {

    private final RedisStorage redis;
    private final Gson gson;

    public ServerService(RedisStorage redis) {
        this.redis = redis;
        this.gson = new Gson();
    }

    public List<Server> getAll() {
        return redis.query(jedis -> {
            Set<String> keys = jedis.keys("kangaroo:servers:*");
            List<Server> servers = new ArrayList<>();

            for (String key : keys) {
                String json = jedis.get(key);
                if (json != null) {
                    servers.add(gson.fromJson(json, Server.class));
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
}
