package gg.desolve.kangaroo.deployment;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Deployer {

    private static final Gson GSON = new Gson();
    private static final Type ROOT_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private Deployer() {
    }

    public static Map<String, List<DeploymentTarget>> loadConfig(File rootDir) {
        File file = new File(rootDir, "servers.json");
        if (!file.isFile()) {
            throw new IllegalStateException(
                    "missing servers.json in " + rootDir.getAbsolutePath()
                            + " — copy servers.example.json and fill it in");
        }
        try {
            return parseConfig(Files.readString(file.toPath()));
        } catch (IOException e) {
            throw new IllegalStateException("failed to read " + file.getAbsolutePath(), e);
        }
    }

    public static Map<String, List<DeploymentTarget>> parseConfig(String json) {
        Map<String, Object> root;
        try {
            root = GSON.fromJson(json, ROOT_TYPE);
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("invalid JSON: " + e.getMessage(), e);
        }
        if (root == null) {
            throw new IllegalArgumentException("config is empty");
        }

        Map<String, List<DeploymentTarget>> groups = new LinkedHashMap<>();
        root.forEach((group, value) -> groups.put(group, parseGroup(group, value)));
        return groups;
    }

    private static List<DeploymentTarget> parseGroup(String group, Object value) {
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("group '" + group + "' must be a JSON array");
        }
        List<DeploymentTarget> targets = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException(
                        "entry #" + i + " in group '" + group + "' must be a JSON object");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> entry = (Map<String, Object>) map;
            targets.add(DeploymentTarget.from(entry));
        }
        return targets;
    }

    public static int deployGroup(String groupKey, File artifact, List<DeploymentTarget> targets, Logger logger) {
        if (targets.isEmpty()) {
            logger.lifecycle("no deployment targets configured for group '" + groupKey + "', skipping");
            return 0;
        }

        logger.lifecycle("deploying " + artifact.getName() + " to " + targets.size()
                + " target(s) in group '" + groupKey + "'");

        int failures = 0;
        for (DeploymentTarget target : targets) {
            long start = System.currentTimeMillis();
            try {
                upload(target, artifact);
                logger.lifecycle("✓ " + target.name() + " (" + (System.currentTimeMillis() - start) + "ms)");
            } catch (Exception e) {
                failures++;
                String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                logger.error("✗ {}: {}", target.name(), reason);
            }
        }
        return failures;
    }

    private static void upload(DeploymentTarget target, File artifact) throws IOException {
        try (SSHClient client = new SSHClient()) {
            client.addHostKeyVerifier(new PromiscuousVerifier());
            client.connect(target.host(), target.port());
            if (target.password() != null) {
                client.authPassword(target.user(), target.password());
            } else {
                client.authPublickey(target.user(), target.privateKey());
            }
            try (SFTPClient sftp = client.newSFTPClient()) {
                sftp.put(artifact.getAbsolutePath(), remoteFilePath(target, artifact));
            }
        }
    }

    private static String remoteFilePath(DeploymentTarget target, File artifact) {
        String dir = target.remotePath();
        if (dir.endsWith("/")) dir = dir.substring(0, dir.length() - 1);
        return dir + "/" + artifact.getName();
    }
}
