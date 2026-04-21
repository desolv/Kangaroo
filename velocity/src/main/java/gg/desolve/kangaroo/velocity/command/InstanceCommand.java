package gg.desolve.kangaroo.velocity.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.velocitypowered.api.proxy.Player;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.velocity.KangarooVelocity;

@CommandAlias("instance|whereami")
@CommandPermission("kangaroo.command.instance")
@Description("View your current proxy and server")
public class InstanceCommand extends BaseCommand {

    @Default
    public void onDefault(Player player) {
        KangarooPlayer tracked = KangarooVelocity.getInstance().getPlayerCache().getByUuid(player.getUniqueId());

        Message.send(player, "<yellow>You are connected to <green>" + tracked.getServer()
                + " <yellow>via <green>" + tracked.getProxy() + "<yellow>.");
    }
}
