package gg.desolve.kangaroo.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ServerEvent {

    private final ServerEventType type;
    private final String serverId;
    private final ServerType serverType;
    private final long loadTimeMs;
    private final long timestamp;

    public String toMessage() {
        String prefix = "<yellow>[Kangaroo] <gray>";
        String color = serverType == ServerType.PROXY ? "<aqua>" : "<green>";

        return switch (type) {
            case CONNECTED -> prefix + "<green>" + color + serverId + " <gray>has connected.";
            case LOADED -> prefix + "<green>" + color + serverId + " <gray>has loaded. <dark_gray>(in "
                    + loadTimeMs + "ms<dark_gray>)";
            case DISCONNECTED -> prefix + "<red>" + serverId + " <gray>was manually stopped.";
            case DIED -> prefix + "<red>" + serverId + " <gray>has died. <dark_gray>(no heartbeat)";
        };
    }
}
