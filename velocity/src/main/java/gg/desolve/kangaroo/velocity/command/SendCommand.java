package gg.desolve.kangaroo.velocity.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import net.kyori.adventure.audience.Audience;

@CommandAlias("send")
@CommandPermission("kangaroo.command.send")
@Description("Send a player to a server")
public class SendCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players @servers")
    public void onDefault(CommandIssuer issuer, KangarooPlayer player, Server target) {
        Audience audience = issuer.getIssuer();

        KangarooVelocity.getInstance().getRedirectService().redirect(player, target.getId());

        Message.send(audience, "<yellow>Sending <green>" + player.getName()
                + " <yellow>to <green>" + target.getId() + "<yellow>.");
    }
}
