package com.github.kr328.gradle.golang;

import com.android.build.api.variant.AndroidComponentsExtension;
import com.android.build.api.variant.DslExtension;
import com.android.build.api.variant.ExternalNativeBuild;
import com.android.build.api.variant.Variant;
import com.android.build.gradle.BaseExtension;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class GoProjectPlugin implements Plugin<Project> {
    private static String capitalize(String str) {
        return Arrays.stream(str.split("[-_]"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining());
    }

    public static File getOutputDir(Project project, String name) {
        return Paths.get(project.getLayout().getBuildDirectory().getAsFile().get().getAbsolutePath(), "outputs", "golang", name)
                .toAbsolutePath().toFile();
    }

    private static void decorateVariant(Project project, Set<String> abiFilters, Variant variant, GoBuildConfig config) {
        final File ndkDirectory = project.getExtensions().getByType(BaseExtension.class).getNdkDirectory();

        for (final String abi : abiFilters) {
            final TaskProvider<GoBuildTask> buildTask = project.getTasks().register(
                    String.format("externalGolangBuild%s[%s]", capitalize(variant.getName()), abi),
                    GoBuildTask.class,
                    (task) -> {
                        task.getNdkDirectory().set(ndkDirectory);

                        if (config.getModuleDirectory() == null) {
                            task.getModuleDir().set(project.file(Paths.get("src", "main", "golang").toString()));
                        } else {
                            task.getModuleDir().set(project.file(config.getModuleDirectory()));
                        }

                        if (config.getPackageName() == null) {
                            task.getPackageName().set("main");
                        } else {
                            task.getPackageName().set(config.getPackageName());
                        }

                        task.getDestinationDir().set(getOutputDir(project, variant.getName()));

                        if (config.getLibraryName() == null) {
                            task.getLibraryName().set("gojni");
                        } else {
                            task.getLibraryName().set(config.getLibraryName());
                        }

                        task.getBuildTags().set(config.getBuildTags());
                        task.getSdkVersion().set(config.getSdkVersion());
                        task.getABI().set(abi);
                        task.getDebuggable().set(config.isDebuggable());
                    }
            );

            // final String externalNativeBuild = "externalNativeBuild" + capitalize(variant.getName());

            // project.getTasks().getByName(externalNativeBuild).dependsOn(buildTask);

            final String preBuild = "pre" + capitalize(variant.getName()) + "Build";

            project.getTasks().getByName(preBuild).dependsOn(buildTask);
            // System.out.println(config);

            project.getTasks().forEach(task -> {
                if (task.getName().startsWith("buildCMake")) {
                    task.mustRunAfter(buildTask);
                }
            });
        }
    }

    @Override
    public void apply(@Nonnull Project target) {
        if (!target.getPlugins().hasPlugin("com.android.base")) {
            throw new GradleException("Android plugin not applied");
        }

        final GoProjectExtension global = target.getExtensions().create("golang", GoProjectExtension.class);
        final AndroidComponentsExtension<?, ?, ?> components = target.getExtensions().getByType(AndroidComponentsExtension.class);

        components.registerExtension(
                new DslExtension.Builder("golang")
                        .extendBuildTypeWith(GoVariantExtension.class)
                        .extendProductFlavorWith(GoVariantExtension.class)
                        .build(),
                (config) -> {
                    final Variant variant = config.getVariant();
                    final ExternalNativeBuild externalNativeBuild = variant.getExternalNativeBuild();

                    if (externalNativeBuild == null) {
                        throw new GradleException("External NDK build is required");
                    }

                    final GoVariantExtension buildType = config.buildTypeExtension(GoVariantExtension.class);
                    final List<GoVariantExtension> productFlavor = config.productFlavorsExtensions(GoVariantExtension.class);

                    final Set<String> abiFilters = externalNativeBuild.getAbiFilters().get();
                    final String moduleDir = global.getModuleDirectory();
                    final String libraryName = global.getLibraryName();
                    final String packageName = global.getPackageName();
                    final boolean isDebuggable = "debug".equals(variant.getBuildType());
                    final Set<String> buildTags = Stream.concat(
                            buildType.getBuildTags().stream(),
                            productFlavor.stream().flatMap(p -> p.getBuildTags().stream())
                    ).collect(Collectors.toSet());

                    /*
                    System.out.println("buildTypeTag");
                    System.out.println(buildType.getBuildTags());
                    System.out.println("productFlavorTag");
                    for (GoVariantExtension extension : productFlavor) {
                        System.out.println(extension.getBuildTags());
                    }

                    System.out.println(buildTags);
                    */

                    return new GoBuildConfig(
                            moduleDir,
                            libraryName,
                            packageName, abiFilters,
                            buildTags,
                            config.getVariant().getMinSdk().getApiLevel(),
                            isDebuggable
                    );
                }
        );

        components.finalizeDsl((dsl) -> {
            dsl.getSourceSets().all((sourceSet) ->
                    sourceSet.getJniLibs().srcDir(getOutputDir(target, sourceSet.getName()))
            );
        });

        components.onVariants(components.selector().all(), (variant) -> {
            final GoBuildConfig config = variant.getExtension(GoBuildConfig.class);
            if (config == null) {
                return;
            }

            target.afterEvaluate((_project) ->
                    decorateVariant(
                            target,
                            Objects.requireNonNull(variant.getExternalNativeBuild()).getAbiFilters().get(),
                            variant,
                            config
                    )
            );
        });
    }
}
