package gg.desolve.kangaroo.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import gg.desolve.kangaroo.bukkit.KangarooBukkit;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.util.Message;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandAlias("servers")
@Description("View all online servers and proxies")
public class ServersCommand extends BaseCommand {

    @Default
    public void onServers(CommandSender sender) {
        List<Server> proxies = KangarooBukkit.getInstance().getServerService().getProxies();
        List<Server> servers = KangarooBukkit.getInstance().getServerService().getServers();

        Message.send(sender, "<dark_gray><st>                              </st>");

        Message.send(sender, "<gold><bold>Proxies</bold> <gray>(" + proxies.size() + ")");
        if (proxies.isEmpty()) {
            Message.send(sender, "  <gray>None online");
        } else {
            for (Server server : proxies) {
                Message.send(sender, formatEntry(server));
            }
        }

        Message.send(sender, "");

        Message.send(sender, "<gold><bold>Servers</bold> <gray>(" + servers.size() + ")");
        if (servers.isEmpty()) {
            Message.send(sender, "  <gray>None online");
        } else {
            for (Server server : servers) {
                Message.send(sender, formatEntry(server));
            }
        }

        Message.send(sender, "<dark_gray><st>                              </st>");
    }

    private String formatEntry(Server server) {
        return "  <green>● <white>" + server.getId() + " <gray>- <white>" + server.getPlayerCount() + "<gray>/<white>" + server.getMaxPlayers() + " players";
    }
}
