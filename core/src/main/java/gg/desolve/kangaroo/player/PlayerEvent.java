package gg.desolve.kangaroo.player;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PlayerEvent {

    private final PlayerEventType type;
    private final UUID uuid;
    private final String name;
    private final String proxy;
    private final String server;
    private final String lastServer;
    private final long timestamp;

    public static PlayerEvent login(KangarooPlayer player) {
        return new PlayerEvent(
                PlayerEventType.LOGIN,
                player.getUuid(),
                player.getName(),
                player.getProxy(),
                player.getServer(),
                null,
                System.currentTimeMillis()
        );
    }

    public static PlayerEvent logout(UUID uuid, String name, String proxy) {
        return new PlayerEvent(
                PlayerEventType.LOGOUT,
                uuid,
                name,
                proxy,
                null,
                null,
                System.currentTimeMillis()
        );
    }

    public static PlayerEvent switchServer(UUID uuid, String name, String proxy,
                                           String newServer, String lastServer, long switchTime) {
        return new PlayerEvent(
                PlayerEventType.SWITCH,
                uuid,
                name,
                proxy,
                newServer,
                lastServer,
                switchTime
        );
    }
}
