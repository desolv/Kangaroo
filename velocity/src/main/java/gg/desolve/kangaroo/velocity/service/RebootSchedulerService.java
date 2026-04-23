package gg.desolve.kangaroo.velocity.service;

import gg.desolve.kangaroo.reboot.ScheduleType;
import gg.desolve.kangaroo.reboot.ScheduledReboot;
import gg.desolve.kangaroo.reboot.ScheduledRebootService;
import gg.desolve.kangaroo.scheduler.KangarooScheduler;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerService;
import gg.desolve.kangaroo.server.ServerType;
import gg.desolve.kangaroo.util.TimeUtil;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class RebootSchedulerService {

    private final ScheduledRebootService rebootService;
    private final ServerService serverService;
    private final RpcService rpcService;
    private final SentinelService sentinelService;
    private final KangarooScheduler scheduler;
    private KangarooScheduler.ScheduledTask task;

    public RebootSchedulerService(ScheduledRebootService rebootService,
                                  ServerService serverService,
                                  RpcService rpcService,
                                  SentinelService sentinelService,
                                  KangarooScheduler scheduler) {
        this.rebootService = rebootService;
        this.serverService = serverService;
        this.rpcService = rpcService;
        this.sentinelService = sentinelService;
        this.scheduler = scheduler;
    }

    public void start() {
        this.task = scheduler.scheduleRepeating(this::tick, 1, 1);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void tick() {
        try {
            if (!sentinelService.isSentinel()) return;
            long now = System.currentTimeMillis();
            for (ScheduledReboot scheduledReboot : rebootService.getAll()) {
                try {
                    process(scheduledReboot, now);
                } catch (Exception exception) {
                    KangarooVelocity.getInstance().getLogger()
                            .error("Failed to process scheduled reboot {}", scheduledReboot.getType(), exception);
                }
            }
        } catch (Exception exception) {
            KangarooVelocity.getInstance().getLogger()
                    .error("Reboot scheduler tick failed", exception);
        }
    }

    private void process(ScheduledReboot scheduledReboot, long now) {
        long delta = scheduledReboot.nextFireAt(now) - now;

        if (delta <= 0 && delta > -1_000L) {
            fire(scheduledReboot);
            return;
        }

        int stage = warnStage(delta);
        if (stage > 0) warn(scheduledReboot.getTarget(), stage);
    }

    private void warn(String target, int seconds) {
        String formattedDuration = TimeUtil.formatDuration(TimeUnit.SECONDS.toMillis(seconds));
        List<Server> targets = resolve(target);

        boolean hasBackend = targets.stream().anyMatch(server -> server.getType() != ServerType.PROXY);

        for (Server server : targets) {
            if (hasBackend && server.getType() == ServerType.PROXY) continue;
            rpcService.broadcastOnServer(
                    server.getId(),
                    "<red>This server will reboot <red><bold>in " + formattedDuration + "."
            );
        }
    }

    private void fire(ScheduledReboot scheduledReboot) {
        for (Server server : resolve(scheduledReboot.getTarget())) {
            rpcService.executeOnServer(
                    server.getId(),
                    server.getType() == ServerType.PROXY ?
                            "end" :
                            "stop"
            );
        }

        if (scheduledReboot.getType() != ScheduleType.DAILY)
            rebootService.delete(scheduledReboot.getType());
    }

    private List<Server> resolve(String target) {
        if (target == null || target.equalsIgnoreCase("global"))
            return serverService.getAll();
        if (target.toLowerCase().startsWith("group:")) {
            String group = target.substring("group:".length());
            return serverService.getAll().stream()
                    .filter(server -> server.getGroups() != null && server.getGroups().contains(group))
                    .toList();
        }
        return serverService.getAll().stream()
                .filter(server -> server.getId().equalsIgnoreCase(target))
                .toList();
    }

    private static int warnStage(long deltaMillis) {
        int maxWarnSeconds = (int) TimeUnit.HOURS.toSeconds(24);
        int[] warnStages = {
                600,
                300,
                180,
                120,
                60,
                30,
                10,
                5,
                4,
                3,
                2,
                1
        };

        if (deltaMillis <= 0) return 0;
        int seconds = (int) ((deltaMillis + 999L) / 1000L);
        if (seconds > maxWarnSeconds) return 0;
        for (int stage : warnStages) {
            if (seconds == stage) return stage;
        }
        return 0;
    }
}
