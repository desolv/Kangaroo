package gg.desolve.kangaroo.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RemoteBroadcast {

    private final String sourceProxy;
    private final String message;
    private final String permission;

}
