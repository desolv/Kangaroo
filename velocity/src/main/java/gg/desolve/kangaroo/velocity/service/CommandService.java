package gg.desolve.kangaroo.velocity.service;

import co.aikar.commands.VelocityCommandManager;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.List;

public class CommandService {

    private final VelocityCommandManager commandManager;

    public CommandService(ProxyServer proxy, Object plugin) {
        this.commandManager = new VelocityCommandManager(proxy, plugin);
    }
}
