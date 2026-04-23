package gg.desolve.kangaroo.velocity.service;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.VelocityCommandManager;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.util.Duration;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import gg.desolve.kangaroo.velocity.command.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CommandService {

    private final VelocityCommandManager commandManager;

    public CommandService() {
        KangarooVelocity plugin = KangarooVelocity.getInstance();
        this.commandManager = new VelocityCommandManager(plugin.getServer(), plugin);
        this.commandManager.enableUnstableAPI("help");

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
                new ExecuteCommand(),
                new ScheduledRebootCommand()
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

        commandManager.getCommandContexts().registerContext(Duration.class, c -> {
            String input = c.popFirstArg();
            try {
                return Duration.parse(input);
            } catch (IllegalArgumentException exception) {
                throw new InvalidCommandArgument("Invalid duration - expected e.g. 30s, 15m, 2h, 1d.");
            }
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

        commandManager.getCommandCompletions().registerCompletion("reboot-targets", context -> {
            List<Server> servers = KangarooVelocity.getInstance().getServerService().getAll();
            List<String> completions = new ArrayList<>();
            completions.add("global");
            Set<String> groups = new LinkedHashSet<>();
            for (Server server : servers) {
                completions.add(server.getId());
                if (server.getGroups() != null) groups.addAll(server.getGroups());
            }
            for (String group : groups) completions.add("group:" + group);
            return completions;
        });
    }
}
