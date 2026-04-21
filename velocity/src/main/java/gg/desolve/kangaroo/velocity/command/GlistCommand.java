package gg.desolve.kangaroo.velocity.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import gg.desolve.kangaroo.player.KangarooPlayer;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.velocity.KangarooVelocity;
import net.kyori.adventure.audience.Audience;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CommandAlias("glist")
@CommandPermission("kangaroo.command.glist")
@Description("View all online players across the network")
public class GlistCommand extends BaseCommand {

    @Default
    public void onDefault(CommandIssuer issuer) {
        Audience audience = issuer.getIssuer();
        List<KangarooPlayer> allPlayers = KangarooVelocity.getInstance().getPlayerCache().getAll();

        Message.send(audience, "<yellow>There are <green>" + allPlayers.size() + " <yellow>players online.");

        Map<String, List<KangarooPlayer>> byServer = allPlayers.stream()
                .collect(Collectors.groupingBy(KangarooPlayer::getServer));

        byServer.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String names = entry.getValue().stream()
                            .map(KangarooPlayer::getName)
                            .sorted()
                            .collect(Collectors.joining("<gray>, <white>"));

                    Message.send(audience, "<yellow>[" + entry.getKey() + "] <white>" + names);
                });
    }
}
