package io.freefair.gradle.plugins;

import io.freefair.gradle.plugins.base.AbstractExtensionPlugin;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileTree;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.util.concurrent.Callable;

import static org.codehaus.groovy.runtime.StringGroovyMethods.capitalize;
import static org.codehaus.groovy.runtime.StringGroovyMethods.minus;

/**
 * @author Lars Grefer
 */
@SuppressWarnings("unused")
public class ExplodedArchivesPlugin extends AbstractExtensionPlugin<ExplodedArchivesExtension> {

    private Task explodedArchivesTask;

    @Override
    public void apply(Project project) {
        super.apply(project);

        explodedArchivesTask = project.getTasks().create("explode");
        explodedArchivesTask.setGroup(BasePlugin.BUILD_GROUP);
    }

    @Override
    protected Class<ExplodedArchivesExtension> getExtensionClass() {
        return ExplodedArchivesExtension.class;
    }

    @Override
    protected void afterEvaluate(final Project project) {
        super.afterEvaluate(project);

        project.getTasks().withType(AbstractArchiveTask.class, new Action<AbstractArchiveTask>() {
            @Override
            public void execute(final AbstractArchiveTask aat) {
                Sync explodedArchiveTask = project.getTasks().create("explode" + capitalize(aat.getName()), Sync.class);

                explodedArchivesTask.dependsOn(explodedArchiveTask);
                explodedArchiveTask.dependsOn(aat);

                explodedArchiveTask.setGroup(aat.getGroup());

                explodedArchiveTask.setDestinationDir(project.file(new Callable<File>() {
                    @Override
                    public File call() throws Exception {
                        return new File(aat.getDestinationDir(), "exploded");
                    }
                }));

                explodedArchiveTask.from(new Callable<FileTree>() {
                    @Override
                    public FileTree call() throws Exception {
                        return project.zipTree(aat.getArchivePath());
                    }
                });

                explodedArchiveTask.into(new Callable<File>() {
                    @Override
                    public File call() throws Exception {

                        File explodedDir = new File(aat.getDestinationDir(), "exploded");
                        if (extension.includeExtension) {
                            return new File(explodedDir, aat.getArchiveName());
                        } else {
                            return new File(explodedDir, minus(aat.getArchiveName(), "." + aat.getExtension()));
                        }
                    }
                });
            }
        });
    }
}