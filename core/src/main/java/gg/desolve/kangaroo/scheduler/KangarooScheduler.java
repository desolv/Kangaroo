package gg.desolve.kangaroo.scheduler;

public interface KangarooScheduler {

    ScheduledTask scheduleRepeating(Runnable task, long initialDelaySeconds, long periodSeconds);

    interface ScheduledTask {
        void cancel();
    }
}
