package gg.desolve.kangaroo.velocity.service;

import gg.desolve.kangaroo.scheduler.KangarooScheduler;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import lombok.Getter;
import redis.clients.jedis.params.SetParams;

public class SentinelService {

    private KangarooScheduler.ScheduledTask task;

    @Getter
    private volatile boolean isSentinel;

    public void start() {
        this.task = KangarooVelocity.getInstance().getScheduler().scheduleRepeating(this::tick, 0, 5);
    }

    public void stop() {
        if (task != null) task.cancel();
        try {
            String myId = KangarooVelocity.getInstance().getProxyId();
            KangarooVelocity.getInstance().getRedisStorage().execute(jedis -> {
                if (myId.equals(jedis.get("kangaroo:sentinel"))) {
                    jedis.del("kangaroo:sentinel");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.isSentinel = false;
    }

    private void tick() {
        try {
            String myId = KangarooVelocity.getInstance().getProxyId();

            this.isSentinel = KangarooVelocity.getInstance().getRedisStorage().query(jedis -> {
                String current = jedis.get("kangaroo:sentinel");
                if (myId.equals(current)) {
                    jedis.expire("kangaroo:sentinel", 15);
                    return true;
                }
                if (current == null) {
                    return "OK".equals(jedis.set("kangaroo:sentinel", myId, SetParams.setParams().nx().ex(15)));
                }
                return false;
            });
        } catch (Exception e) {
            this.isSentinel = false;
            e.printStackTrace();
        }
    }
}
