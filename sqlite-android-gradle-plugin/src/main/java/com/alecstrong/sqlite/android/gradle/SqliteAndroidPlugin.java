package com.alecstrong.sqlite.android.gradle;

import com.alecstrong.sqlite.android.SqliteCompiler;
import com.android.build.gradle.AppPlugin;
import com.google.common.base.CaseFormat;
import java.util.Arrays;
import javax.inject.Inject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.FileResolver;

public class SqliteAndroidPlugin implements Plugin<Project> {
  private final FileResolver fileResolver;

  @Inject
  public SqliteAndroidPlugin(FileResolver fileResolver) {
    this.fileResolver = fileResolver;
  }

  @Override
  public void apply(Project project) {
    project.getDependencies().add("compile", "com.android.support:support-annotations:23.1.0");

    project.afterEvaluate(afterEvaluateProject -> {
      ((AppPlugin) afterEvaluateProject.getPlugins().findPlugin("android")).getVariantManager()
          .getVariantDataList()
          .forEach(variant -> {
            // Get .sqlite files.
            SourceDirectorySet sqliteSources =
                new DefaultSourceDirectorySet(variant.getName(), fileResolver);
            sqliteSources.getFilter().include("**/*." + SqliteCompiler.getFileExtension());
            sqliteSources.srcDirs("src");

            // Set up the generateSql task.
            String taskName = "generate"
                + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, variant.getName())
                + "SQLiteInterface";
            SqliteAndroidTask task = project.getTasks().create(taskName, SqliteAndroidTask.class);
            task.setGroup("sqlite");
            task.setBuildDirectory(project.getBuildDir());
            task.setDescription(String.format(
                "Generate Android interfaces for working with %s sqlite tables",
                variant.getName()));
            task.setSource(sqliteSources);

            // Update the variant to include the sqlite task.
            variant.preBuildTask.dependsOn(taskName);
            variant.addJavaSourceFoldersToModel(task.getOutputDirectory());
            variant.javacTask.getOptions().getCompilerArgs().addAll(
                Arrays.asList("-sourcepath", String.valueOf(task.getOutputDirectory())));
          });
    });
  }
}
