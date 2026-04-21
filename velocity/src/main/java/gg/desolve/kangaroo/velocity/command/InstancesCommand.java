package gg.desolve.kangaroo.velocity.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerService;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import net.kyori.adventure.audience.Audience;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandAlias("instances")
@CommandPermission("kangaroo.command.instances")
@Description("List all live proxy instances")
public class InstancesCommand extends BaseCommand {

    @Default
    public void onDefault(CommandIssuer issuer) {
        Audience audience = issuer.getIssuer();
        ServerService serverService = KangarooVelocity.getInstance().getServerService();

        List<Server> proxies = serverService.getProxies();
        Map<String, Long> playerCounts = KangarooVelocity.getInstance().getPlayerCache().getAll().stream()
                .collect(Collectors.groupingBy(KangarooPlayer::getProxy, Collectors.counting()));

        String sentinel = serverService.getSentinelProxy().map(Server::getId).orElse(null);

        Message.send(audience, "<yellow>There are <green>" + proxies.size()
                + " <yellow>proxy instances online.");

        for (Server proxy : proxies.stream().sorted(Comparator.comparing(Server::getId)).toList()) {
            long count = playerCounts.getOrDefault(proxy.getId(), 0L);
            String sentinelTag = proxy.getId().equals(sentinel) ? " <gold>(sentinel)" : "";
            Message.send(audience, "<aqua>[" + proxy.getId() + "]<gray>: <white>"
                    + count + " players" + sentinelTag);
        }
    }
}
