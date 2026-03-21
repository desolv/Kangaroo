package gg.desolve.llama.storage;

import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.function.Consumer;
import java.util.function.Function;

public class RedisStorage {

    @Getter
    private final JedisPool pool;

    public RedisStorage(String host, int port, String password) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(16);
        config.setMaxIdle(8);

        if (password != null && !password.isEmpty()) {
            this.pool = new JedisPool(config, host, port, 2000, password);
        } else {
            this.pool = new JedisPool(config, host, port);
        }
    }

    public void execute(Consumer<Jedis> action) {
        try (Jedis jedis = pool.getResource()) {
            action.accept(jedis);
        }
    }

    public <T> T query(Function<Jedis, T> action) {
        try (Jedis jedis = pool.getResource()) {
            return action.apply(jedis);
        }
    }

    public void close() {
        pool.close();
    }
}
