package gg.desolve.llama.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(
        id = "llama",
        name = "Llama",
        version = "1.0"
)
public final class LlamaVelocity {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public LlamaVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initialising Llama for velocity server...");
    }
}
