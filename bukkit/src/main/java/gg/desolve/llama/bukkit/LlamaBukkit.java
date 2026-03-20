package gg.desolve.llama.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

public final class LlamaBukkit extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getLogger().info("Initialising Llama for bukkit server...");
    }

    @Override
    public void onDisable() {
    }
}
