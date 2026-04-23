package gg.desolve.kangaroo.util;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;

import java.util.List;

public class Message {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component translate(String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    public static Component translate(String message, TagResolver... resolvers) {
        return MINI_MESSAGE.deserialize(message, resolvers);
    }

    public static void send(Audience audience, String message) {
        audience.sendMessage(translate(message));
    }

    public static void send(Audience audience, String message, TagResolver... resolvers) {
        audience.sendMessage(translate(message, resolvers));
    }

    public static void send(Audience audience, List<String> lines) {
        audience.sendMessage(translate(String.join("<newline>", lines)));
    }

    public static void broadcast(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> send(player, message));
    }

    public static void broadcast(String message, String permission) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!player.hasPermission(permission)) return;
            send(player, message);
        });
    }
}
