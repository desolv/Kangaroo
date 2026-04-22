package gg.desolve.kangaroo.deployment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public record DeploymentTarget(
        String name,
        String host,
        int port,
        String user,
        String password,
        String privateKey,
        String remotePath
) {

    public static final int DEFAULT_PORT = 2022;
    public static final String DEFAULT_REMOTE_PATH = "/plugins";

    public static DeploymentTarget from(Map<String, Object> raw) {
        String name = str(raw, "name");
        require(name, "target is missing 'name': " + raw);

        String host = str(raw, "host");
        require(host, "target '" + name + "' is missing 'host'");

        String user = str(raw, "user");
        require(user, "target '" + name + "' is missing 'user'");

        String password = str(raw, "password");
        String privateKey = str(raw, "privateKey");
        if ((password == null) == (privateKey == null)) {
            throw new IllegalArgumentException(
                    "target '" + name + "' must set exactly one of 'password' or 'privateKey'");
        }

        if (privateKey != null) {
            Path resolved = expandTilde(privateKey);
            if (!Files.isRegularFile(resolved)) {
                throw new IllegalArgumentException(
                        "target '" + name + "' privateKey not found: " + resolved);
            }
            privateKey = resolved.toString();
        }

        int port = raw.get("port") instanceof Number n ? n.intValue() : DEFAULT_PORT;
        String remotePath = str(raw, "remotePath");
        if (remotePath == null) remotePath = DEFAULT_REMOTE_PATH;

        return new DeploymentTarget(name, host, port, user, password, privateKey, remotePath);
    }

    private static String str(Map<String, Object> raw, String key) {
        Object v = raw.get(key);
        if (v == null) return null;
        String s = v.toString();
        return s.isEmpty() ? null : s;
    }

    private static void require(String value, String message) {
        if (value == null) throw new IllegalArgumentException(message);
    }

    private static Path expandTilde(String path) {
        if (path.equals("~")) return Path.of(System.getProperty("user.home"));
        if (path.startsWith("~/")) return Path.of(System.getProperty("user.home"), path.substring(2));
        return Path.of(path);
    }
}
