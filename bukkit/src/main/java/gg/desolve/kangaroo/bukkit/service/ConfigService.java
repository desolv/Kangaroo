package gg.desolve.kangaroo.bukkit.service;

import gg.desolve.kangaroo.storage.ConfigStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigService {

    private final JavaPlugin plugin;
    private final Map<String, ConfigStorage> configs = new HashMap<>();

    public ConfigService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ConfigStorage load(String fileName) {
        return configs.computeIfAbsent(fileName, name -> {
            try {
                return new ConfigStorage(
                        new File(plugin.getDataFolder(), name),
                        plugin.getResource(name)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
