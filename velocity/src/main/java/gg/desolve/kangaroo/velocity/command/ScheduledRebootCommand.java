package gg.desolve.kangaroo.velocity.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import com.velocitypowered.api.proxy.Player;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.reboot.ScheduleType;
import gg.desolve.kangaroo.reboot.ScheduledReboot;
import gg.desolve.kangaroo.reboot.ScheduledRebootService;
import gg.desolve.kangaroo.util.Duration;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.util.TimeUtil;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import net.kyori.adventure.audience.Audience;

import java.util.Optional;
import java.util.regex.Pattern;

@CommandAlias("scheduledreboot")
@CommandPermission("kangaroo.command.scheduledreboot")
@Description("Manage scheduled reboots")
public class ScheduledRebootCommand extends BaseCommand {

    private static final Pattern DAILY_TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    @Default
    @HelpCommand
    public void onHelp(CommandHelp help) {
        help.showHelp();
    }

    @Subcommand("initiate")
    @CommandCompletion("@nothing @reboot-targets")
    @Syntax("<duration> [target]")
    public void onInitiate(CommandIssuer issuer, Duration duration, @co.aikar.commands.annotation.Optional String target) {
        Audience audience = issuer.getIssuer();
        if (!duration.isPositive()) {
            Message.send(audience, "<red>Duration must be positive.");
            return;
        }
        if (duration.isGreaterThan(Duration.ofHours(24))) {
            Message.send(audience, "<red>Duration cannot exceed 24 hours.");
            return;
        }

        ScheduleType type = target == null ? ScheduleType.LOCAL : ScheduleType.GLOBAL;
        String resolvedTarget = target == null ? resolveLocalTarget(issuer) : target;
        if (resolvedTarget == null) {
            Message.send(audience, "<red>Could not resolve your current server. Specify a target.");
            return;
        }

        ScheduledRebootService service = KangarooVelocity.getInstance().getScheduledRebootService();
        if (service.get(type).isPresent()) {
            Message.send(audience, "<red>A " + type.name().toLowerCase() + " reboot is already scheduled. Cancel it first.");
            return;
        }

        long runAt = System.currentTimeMillis() + duration.toMillis();
        service.set(new ScheduledReboot(
                type,
                resolvedTarget,
                null,
                runAt,
                System.currentTimeMillis()
        ));

        Message.send(audience, "<green>A " + type.name().toLowerCase() + " reboot has been initiated for <yellow>"
                + resolvedTarget + " <green>in " + duration.format() + ".");
    }

    private static String resolveLocalTarget(CommandIssuer issuer) {
        Object source = issuer.getIssuer();
        if (source instanceof Player player) {
            KangarooPlayer tracked = KangarooVelocity.getInstance().getPlayerCache().getByUuid(player.getUniqueId());
            return tracked == null ? null : tracked.getServer();
        }
        return KangarooVelocity.getInstance().getProxyId();
    }

    @Subcommand("cancel")
    public void onCancel(CommandIssuer issuer) {
        Audience audience = issuer.getIssuer();
        ScheduledRebootService service = KangarooVelocity.getInstance().getScheduledRebootService();

        ScheduleType cancelled = null;
        if (service.delete(ScheduleType.LOCAL)) cancelled = ScheduleType.LOCAL;
        else if (service.delete(ScheduleType.GLOBAL)) cancelled = ScheduleType.GLOBAL;

        if (cancelled == null) {
            Message.send(audience, "<red>No reboot is currently scheduled.");
            return;
        }

        Message.send(audience, "<green>Removed the scheduled <yellow>" + cancelled.name().toLowerCase() + "<green> reboot.");
    }

    @Subcommand("status")
    public void onStatus(CommandIssuer issuer) {
        Audience audience = issuer.getIssuer();
        ScheduledRebootService service = KangarooVelocity.getInstance().getScheduledRebootService();
        Optional<ScheduledReboot> local = service.get(ScheduleType.LOCAL);
        Optional<ScheduledReboot> global = service.get(ScheduleType.GLOBAL);

        if (local.isEmpty() && global.isEmpty()) {
            Message.send(audience, "<red>No reboot is currently scheduled.");
            return;
        }

        long now = System.currentTimeMillis();
        local.ifPresent(reboot -> Message.send(audience,
                "<green>A local reboot is pending for <yellow>" + reboot.getTarget()
                        + " <green>in " + TimeUtil.formatDuration(reboot.getRunAt() - now) + "."));
        global.ifPresent(reboot -> Message.send(audience,
                "<green>A global reboot is pending for <yellow>" + reboot.getTarget()
                        + " <green>in " + TimeUtil.formatDuration(reboot.getRunAt() - now) + "."));
    }

    @Subcommand("schedule")
    public void onSchedule(CommandIssuer issuer) {
        Audience audience = issuer.getIssuer();
        ScheduledRebootService service = KangarooVelocity.getInstance().getScheduledRebootService();
        ScheduledReboot daily = service.get(ScheduleType.DAILY).orElseThrow();

        Message.send(audience, "<green>Daily reboot is scheduled at <yellow>" + daily.getDailyTime() + "<green>.");
    }

    @Subcommand("reschedule")
    @Syntax("<HH:mm>")
    public void onReschedule(CommandIssuer issuer, String time) {
        Audience audience = issuer.getIssuer();
        if (!DAILY_TIME_PATTERN.matcher(time).matches()) {
            Message.send(audience, "<red>Time must be in HH:mm format (00:00 - 23:59).");
            return;
        }

        ScheduledRebootService service = KangarooVelocity.getInstance().getScheduledRebootService();
        service.set(new ScheduledReboot(
                ScheduleType.DAILY,
                "global",
                time,
                null,
                System.currentTimeMillis()
        ));

        Message.send(audience, "<green>Daily reboot set to <yellow>" + time + "<green>.");
    }
}
