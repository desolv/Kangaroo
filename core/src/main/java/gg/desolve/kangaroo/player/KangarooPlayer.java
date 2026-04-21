package gg.desolve.kangaroo.player;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class KangarooPlayer {

    private final UUID uuid;
    private final String name;
    private final String proxy;
    private String server;
    private final long loginTime;
    private String lastServer;
    private long lastSwitchTime;
    private long lastHeartbeat;

}
