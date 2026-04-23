package gg.desolve.kangaroo.velocity.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import net.kyori.adventure.audience.Audience;

@CommandAlias("sendall")
@CommandPermission("kangaroo.command.sendall")
@Description("Send every online player to a server")
public class SendAllCommand extends BaseCommand {

    @Default
    @CommandCompletion("@servers")
    public void onDefault(CommandIssuer issuer, Server target) {
        Audience audience = issuer.getIssuer();

        int count = 0;
        for (KangarooPlayer player : KangarooVelocity.getInstance().getPlayerCache().getAll()) {
            KangarooVelocity.getInstance().getRedirectService().redirect(player, target.getId());
            count++;
        }

        Message.send(audience, "<yellow>Sending <green>" + count
                + " <yellow>players to <green>" + target.getId() + "<yellow>.");
    }
}
