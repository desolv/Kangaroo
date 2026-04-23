package gg.desolve.kangaroo.storage;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.net.URI;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class RedisStorage {

    private final JedisPool pool;

    public RedisStorage(String uri) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(16);
        config.setMaxIdle(8);
        config.setTestOnBorrow(false);
        config.setBlockWhenExhausted(true);

        this.pool = new JedisPool(
                config,
                URI.create(uri),
                3_000,
                3_000
        );

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

    public void publish(String channel, String message) {
        execute(jedis -> jedis.publish(channel, message));
    }

    public Subscription subscribe(String channel, BiConsumer<String, String> listener) {
        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String ch, String message) {
                listener.accept(ch, message);
            }
        };

        Thread thread = new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(pubSub, channel);
            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    e.printStackTrace();
                }
            }
        }, "kangaroo-sub-" + channel);
        thread.setDaemon(true);
        thread.start();

        return () -> {
            try {
                pubSub.unsubscribe();
            } catch (Exception ignored) {
            }
            thread.interrupt();
        };
    }

    public void close() {
        pool.close();
    }

    @FunctionalInterface
    public interface Subscription {
        void unsubscribe();
    }
}
