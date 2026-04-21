package gg.desolve.kangaroo.velocity.service;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.VelocityCommandManager;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import gg.desolve.kangaroo.velocity.command.ExecuteCommand;
import gg.desolve.kangaroo.velocity.command.FindCommand;
import gg.desolve.kangaroo.velocity.command.GlistCommand;
import gg.desolve.kangaroo.velocity.command.InstanceCommand;
import gg.desolve.kangaroo.velocity.command.InstancesCommand;
import gg.desolve.kangaroo.velocity.command.SendAllCommand;
import gg.desolve.kangaroo.velocity.command.SendCommand;
import gg.desolve.kangaroo.velocity.command.TrackedPlayerCommand;

import java.util.List;

public class CommandService {

    private final VelocityCommandManager commandManager;

    public CommandService() {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        this.commandManager = new VelocityCommandManager(plugin.getServer(), plugin);

        registerContexts();
        registerCompletions();

        List.of(
                new GlistCommand(),
                new FindCommand(),
                new TrackedPlayerCommand(),
                new InstanceCommand(),
                new InstancesCommand(),
                new SendCommand(),
                new SendAllCommand(),
                new ExecuteCommand()
        ).forEach(commandManager::registerCommand);
    }

    private void registerContexts() {
        commandManager.getCommandContexts().registerContext(KangarooPlayer.class, c -> {
            String name = c.popFirstArg();
            KangarooPlayer player = KangarooVelocity.getInstance().getPlayerCache().getByName(name);
            if (player == null) {
                throw new InvalidCommandArgument("No player named '" + name + "' is online.");
            }
            return player;
        });

        commandManager.getCommandContexts().registerContext(Server.class, c -> {
            String id = c.popFirstArg();
            return KangarooVelocity.getInstance().getServerService().getById(id)
                    .orElseThrow(() -> new InvalidCommandArgument("No server found with id '" + id + "'."));
        });
    }

    private void registerCompletions() {
        commandManager.getCommandCompletions().registerCompletion("players", c ->
                KangarooVelocity.getInstance().getPlayerCache().getAll().stream()
                        .map(KangarooPlayer::getName).toList());

        commandManager.getCommandCompletions().registerCompletion("servers", c ->
                KangarooVelocity.getInstance().getServerService().getServers().stream()
                        .map(Server::getId).toList());

        commandManager.getCommandCompletions().registerCompletion("proxies", c ->
                KangarooVelocity.getInstance().getServerService().getProxies().stream()
                        .map(Server::getId).toList());
    }
}
