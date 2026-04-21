package gg.desolve.kangaroo.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RedirectRequest {

    private final UUID playerId;
    private final String targetServer;

}
