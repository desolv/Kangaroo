package gg.desolve.kangaroo.bukkit.inventory;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import gg.desolve.kangaroo.bukkit.KangarooBukkit;
import gg.desolve.kangaroo.bukkit.util.GuiUtil;
import gg.desolve.kangaroo.server.Server;
import gg.desolve.kangaroo.server.ServerType;
import gg.desolve.kangaroo.util.Message;
import gg.desolve.kangaroo.util.TimeUtil;
import me.lucko.helper.Schedulers;
import me.lucko.helper.scheduler.Task;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServersInventory {

    public ServersInventory(Player player) {
        PaginatedGui gui = Gui.paginated()
                .title(Message.translate("Overview"))
                .rows(4)
                .pageSize(18)
                .create();

        gui.setDefaultClickAction(e -> e.setCancelled(true));

        gui.getFiller().fillTop(
                new GuiItem(
                        GuiUtil.item(
                                Material.GRAY_STAINED_GLASS_PANE,
                                " "
                        )));

        populate(gui);
        gui.open(player);

        Task refresh = Schedulers.builder()
                .sync()
                .every(1, TimeUnit.SECONDS)
                .run(() -> {
                    populate(gui);
                    gui.update();
                });

        gui.setCloseGuiAction(e -> refresh.close());
    }

    private void populate(PaginatedGui gui) {
        gui.clearPageItems();

        KangarooBukkit.getInstance().getServerService().getAll().forEach(server ->
                gui.addItem(serverItem(server)));

        if (gui.getCurrentPageNum() > 1) {
            gui.setItem(
                    1,
                    1,
                    new GuiItem(
                            GuiUtil.item(
                                    Material.ARROW,
                                    "<gray>Previous Page"
                            ),
                            e -> {
                                gui.previous();
                                populate(gui);
                                gui.update();
                            }));
        }

        if (gui.getCurrentPageNum() < gui.getPagesNum()) {
            gui.setItem(
                    1,
                    9,
                    new GuiItem(
                            GuiUtil.item(
                                    Material.ARROW,
                                    "<gray>Next Page"
                            ),
                            e -> {
                                gui.next();
                                populate(gui);
                                gui.update();
                            }));
        }
    }

    private GuiItem serverItem(Server server) {
        boolean isProxy = server.getType() == ServerType.PROXY;
        Material material = isProxy ? Material.CYAN_WOOL : Material.LIME_WOOL;
        String color = isProxy ? "<aqua>" : "<green>";

        String heartbeatText = server.getLastHeartbeat() > 0
                ? TimeUtil.secondsAgo(server.getLastHeartbeat()) + "s ago"
                : "Unknown";

        ItemStack stack = new ItemStack(material);
        stack.editMeta(meta -> {
            meta.displayName(GuiUtil.text(color + "<bold>" + server.getId()));

            meta.lore(List.of(
                    GuiUtil.text("<dark_gray>Last heartbeat " + heartbeatText + ".."),
                    Component.empty(),
                    GuiUtil.text("<gray>Type: <white>" + server.getType().name()),
                    GuiUtil.text("<gray>Software: <white>" + server.getSoftware()),
                    GuiUtil.text("<gray>Uptime: <white>" + TimeUtil.formatUptime(server.getStartTime())),
                    GuiUtil.text(isProxy
                            ? "<gray>Players: <white>" + server.getPlayerCount()
                            : "<gray>Players: <white>" + server.getPlayerCount() + "/" + server.getMaxPlayers()),
                    !isProxy ? GuiUtil.text("<gray>TPS: <white>" + String.format("%.1f", server.getTps())) : Component.empty(),
                    Component.empty(),
                    GuiUtil.text("<gray>Host: <white>" + server.getHost() + ":" + server.getPort())
            ));
        });

        return new GuiItem(stack);
    }
}
