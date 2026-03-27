package gg.desolve.kangaroo.bukkit.util;

import gg.desolve.kangaroo.util.Message;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GuiUtil {

    public static ItemStack item(Material material, String name) {
        ItemStack stack = new ItemStack(material);
        stack.editMeta(meta -> meta.displayName(text(name)));
        return stack;
    }

    public static Component text(String minimessage) {
        return Message.translate("<!italic>" + minimessage);
    }
}
