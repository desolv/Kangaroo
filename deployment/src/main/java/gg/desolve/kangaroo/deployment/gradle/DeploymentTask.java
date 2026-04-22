package gg.desolve.kangaroo.deployment.gradle;

import gg.desolve.kangaroo.deployment.Deployer;
import gg.desolve.kangaroo.deployment.DeploymentTarget;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class DeploymentTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NAME_ONLY)
    public abstract RegularFileProperty getArtifact();

    @Input
    public abstract Property<String> getGroupKey();

    @Internal
    public File getConfigRoot() {
        return getProject().getRootDir();
    }

    @TaskAction
    public void deploy() {
        String key = getGroupKey().get();
        File artifact = getArtifact().get().getAsFile();

        Map<String, List<DeploymentTarget>> config;
        try {
            config = Deployer.loadConfig(getConfigRoot());
        } catch (RuntimeException e) {
            throw new GradleException(e.getMessage(), e);
        }

        List<DeploymentTarget> targets = config.getOrDefault(key, List.of());
        int failures = Deployer.deployGroup(key, artifact, targets, getLogger());
        if (failures > 0) {
            throw new GradleException("deployment failed for " + failures + " target(s) in group '" + key + "'");
        }
    }
}
