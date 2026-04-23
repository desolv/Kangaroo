package gg.desolve.kangaroo.deployment.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

public class DeploymentPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        DeploymentExtension extension = project.getExtensions()
                .create("deployment", DeploymentExtension.class);

        TaskProvider<DeploymentTask> taskProvider = project.getTasks()
                .register("publishPlugin", DeploymentTask.class, task -> {
                    task.setGroup("publishing");
                    task.setDescription("Upload the shadowed plugin jar over SFTP to every configured target");
                    task.getGroupKey().set(extension.getGroupKey());
                });

        project.afterEvaluate(evaluatedProject -> {
            TaskProvider<Jar> shadow = evaluatedProject.getTasks().named("shadowJar", Jar.class);
            TaskProvider<Jar> jar = evaluatedProject.getTasks().named("jar", Jar.class);
            taskProvider.configure(task -> {
                task.getArtifact().set(shadow.flatMap(Jar::getArchiveFile));
                task.dependsOn(shadow, jar);
            });
        });
    }
}
