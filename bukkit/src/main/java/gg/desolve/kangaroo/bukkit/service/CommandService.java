package gg.desolve.kangaroo.bukkit.service;

import co.aikar.commands.PaperCommandManager;
import gg.desolve.kangaroo.bukkit.command.ServersCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class CommandService {

    private final PaperCommandManager commandManager;

    public CommandService(JavaPlugin plugin) {
        this.commandManager = new PaperCommandManager(plugin);

        List.of(
                new ServersCommand()
        ).forEach(commandManager::registerCommand);
    }
}
