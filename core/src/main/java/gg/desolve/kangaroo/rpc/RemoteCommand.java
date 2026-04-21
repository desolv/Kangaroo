package gg.desolve.kangaroo.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RemoteCommand {

    private final String sourceProxy;
    private final String command;

}
