package gg.desolve.kangaroo.velocity.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.util.TimeUtil;
import net.kyori.adventure.audience.Audience;

import java.util.List;

@CommandAlias("trackedplayer")
@CommandPermission("kangaroo.command.trackedplayer")
@Description("View detailed info about a tracked player")
public class TrackedPlayerCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players")
    public void onDefault(CommandIssuer issuer, KangarooPlayer player) {
        Audience audience = issuer.getIssuer();

        Message.send(audience, List.of(
                "<aqua><bold>Tracked Kangaroo Player</bold>",
                "<gray>UUID: <white>" + player.getUuid(),
                "<gray>Username: <white>" + player.getName(),
                "<gray>Proxy: <white>" + player.getProxy(),
                "<gray>Server: <white>" + player.getServer(),
                "<gray>Online for: <white>" + TimeUtil.formatUptime(player.getLoginTime()),
                "<gray>Last server: <white>" + player.getLastServer(),
                "<gray>Last switch: <white>" + TimeUtil.secondsAgo(player.getLastSwitchTime()) + "s ago",
                "<gray>Last heartbeat: <white>" + TimeUtil.secondsAgo(player.getLastHeartbeat()) + "s ago"
        ));
    }
}
