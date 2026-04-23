package gg.desolve.kangaroo.bukkit.scheduler;

import gg.desolve.kangaroo.scheduler.KangarooScheduler;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;

import java.util.concurrent.TimeUnit;

public class HelperScheduler implements KangarooScheduler {

    @Override
    public ScheduledTask scheduleRepeating(Runnable task, long initialDelaySeconds, long periodSeconds) {
        Task helperTask = Schedulers.builder()
                .async()
                .after(initialDelaySeconds, TimeUnit.SECONDS)
                .every(periodSeconds, TimeUnit.SECONDS)
                .run(task);
        return helperTask::close;
    }
}
