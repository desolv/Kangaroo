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

import java.util.ArrayList;
import java.util.Comparator;
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

        KangarooBukkit.getInstance().getServerService().getAll().stream()
                .sorted(Comparator.comparingInt(server -> server.getType() == ServerType.PROXY ? 0 : 1))
                .forEach(server -> gui.addItem(serverItem(server)));

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

            List<Component> lore = new ArrayList<>();
            lore.add(GuiUtil.text("<dark_gray>Last heartbeat " + heartbeatText + ".."));
            lore.add(Component.empty());

            lore.add(GuiUtil.text(color + "Server Identifier"));
            lore.add(GuiUtil.text("<gray>Uptime: <white>" + TimeUtil.formatUptime(server.getStartTime())));
            lore.add(GuiUtil.text("<gray>Groups:"));
            server.getGroups().forEach(group -> lore.add(GuiUtil.text("<gray>- <white>" + group)));
            lore.add(Component.empty());

            lore.add(GuiUtil.text(color + "Network"));
            lore.add(GuiUtil.text("<gray>Host: <white>" + server.getHost()));
            lore.add(GuiUtil.text("<gray>Port: <white>" + server.getPort()));
            lore.add(GuiUtil.text(isProxy
                    ? "<gray>Players: <white>" + server.getTotalPlayers()
                    : "<gray>Players: <white>" + server.getTotalPlayers() + "/" + server.getMaxPlayers()));
            lore.add(Component.empty());

            lore.add(GuiUtil.text(color + "Performance"));
            lore.add(GuiUtil.text("<gray>Software: <white>" + server.getSoftware()));
            lore.add(GuiUtil.text("<gray>CPU: <white>" + String.format("%.1f", server.getCpu()) + "%"));
            if (!isProxy)
                lore.add(GuiUtil.text("<gray>TPS: <white>" + String.format("%.1f", server.getTps())));

            meta.lore(lore);
        });

        return new GuiItem(stack);
    }
}
