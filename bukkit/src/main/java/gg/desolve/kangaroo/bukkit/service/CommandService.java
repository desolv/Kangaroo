package gg.desolve.kangaroo.bukkit.service;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import gg.desolve.kangaroo.bukkit.KangarooBukkit;
import gg.desolve.kangaroo.bukkit.command.ServersCommand;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.server.Server;

public class CommandService {

    private final PaperCommandManager commandManager;

    public CommandService() {
        this.commandManager = new PaperCommandManager(KangarooBukkit.getInstance());
        this.commandManager.enableUnstableAPI("help");

        registerContexts();
        registerCompletions();

        commandManager.registerCommand(new ServersCommand());
    }

    private void registerContexts() {
        commandManager.getCommandContexts().registerContext(KangarooPlayer.class, context -> {
            String name = context.popFirstArg();
            KangarooPlayer player = KangarooBukkit.getInstance().getPlayerCache().getByName(name);
            if (player == null) {
                throw new InvalidCommandArgument("No player named '" + name + "' is online.");
            }
            return player;
        });

        commandManager.getCommandContexts().registerContext(Server.class, context -> {
            String id = context.popFirstArg();
            return KangarooBukkit.getInstance().getServerService().getById(id)
                    .orElseThrow(() -> new InvalidCommandArgument("No server found with id '" + id + "'."));
        });
    }

    private void registerCompletions() {
        commandManager.getCommandCompletions().registerCompletion("players", context ->
                KangarooBukkit.getInstance().getPlayerCache().getAll().stream()
                        .map(KangarooPlayer::getName).toList());

        commandManager.getCommandCompletions().registerCompletion("servers", context ->
                KangarooBukkit.getInstance().getServerService().getServers().stream()
                        .map(Server::getId).toList());

        commandManager.getCommandCompletions().registerCompletion("proxies", context ->
                KangarooBukkit.getInstance().getServerService().getProxies().stream()
                        .map(Server::getId).toList());
    }
}
