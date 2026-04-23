package gg.desolve.kangaroo.deployment.gradle;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public interface DeploymentExtension {

    Property<String> getGroupKey();

    RegularFileProperty getConfigFile();
}
