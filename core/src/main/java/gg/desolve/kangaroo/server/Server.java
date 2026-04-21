package gg.desolve.kangaroo.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Server {

    private final String id;
    private final ServerType type;
    private int totalPlayers;
    private int maxPlayers;
    private final String host;
    private final int port;
    private final String software;
    private double tps;
    private double cpu;
    private final long startTime;
    private long lastHeartbeat;

}
