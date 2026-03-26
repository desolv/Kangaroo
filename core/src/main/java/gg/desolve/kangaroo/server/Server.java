package gg.desolve.kangaroo.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Server {

    private final String id;
    private final ServerType type;
    private int playerCount;
    private int maxPlayers;

}
