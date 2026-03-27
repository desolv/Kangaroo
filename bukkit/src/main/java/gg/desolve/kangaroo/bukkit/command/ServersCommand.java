package gg.desolve.kangaroo.bukkit.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import gg.desolve.kangaroo.bukkit.inventory.ServersInventory;
import org.bukkit.entity.Player;

@CommandAlias("servers")
@CommandPermission("kangaroo.command.servers")
@Description("View all online servers and proxies")
public class ServersCommand extends BaseCommand {

    @Default
    public void onDefault(Player player) {
        new ServersInventory(player);
    }
}
