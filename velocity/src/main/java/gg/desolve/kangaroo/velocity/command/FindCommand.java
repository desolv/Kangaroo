package gg.desolve.kangaroo.velocity.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.util.Message;
import net.kyori.adventure.audience.Audience;

@CommandAlias("find")
@CommandPermission("kangaroo.command.find")
@Description("Find which server a player is on")
public class FindCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players")
    public void onDefault(CommandIssuer issuer, KangarooPlayer player) {
        Audience audience = issuer.getIssuer();

        Message.send(audience, "<yellow>" + player.getName() + " is connected to <green>"
                + player.getServer()
                + " <yellow>via <green>" + player.getProxy() + "<yellow>.");
    }
}
