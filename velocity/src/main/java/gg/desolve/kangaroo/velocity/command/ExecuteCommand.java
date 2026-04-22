package gg.desolve.kangaroo.velocity.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerType;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import net.kyori.adventure.audience.Audience;

@CommandAlias("execute")
@CommandPermission("kangaroo.command.execute")
@Description("Execute a command on a bukkit server")
public class ExecuteCommand extends BaseCommand {

    @Default
    @CommandCompletion("@servers")
    public void onDefault(CommandIssuer issuer, Server target, String command) {
        Audience audience = issuer.getIssuer();

        if (target.getType() == ServerType.PROXY) {
            Message.send(audience, "<red>You are not allowed to execute commands on proxy servers.");
            return;
        }

        KangarooVelocity.getInstance().getRpcService().executeOnServer(target.getId(), command);
        Message.send(audience, "<yellow>Executed <green>" + command
                + " <yellow>on <green>" + target.getId() + "<yellow>.");
    }
}
