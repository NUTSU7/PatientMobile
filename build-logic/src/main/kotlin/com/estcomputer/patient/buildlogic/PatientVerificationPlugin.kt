package com.estcomputer.patient.buildlogic

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.configure

class PatientVerificationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            tasks.register<VerifyForbiddenDependenciesTask>(FORBIDDEN_TASK) {
                classpathConfigurations.set(
                    provider {
                        configurations
                            .matching { config ->
                                config.isCanBeResolved &&
                                    (config.name.endsWith("RuntimeClasspath") || config.name.endsWith("CompileClasspath"))
                            }
                            .toList()
                    },
                )
            }

            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationAndroidComponentsExtension> {
                    onVariants(selector().withBuildType(RELEASE_BUILD_TYPE)) { variant ->
                        tasks.register<VerifyReleaseManifestTask>(MANIFEST_TASK) {
                            mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val FORBIDDEN_TASK = "verifyForbiddenDependencies"
        const val MANIFEST_TASK = "verifyReleaseManifest"
        const val RELEASE_BUILD_TYPE = "release"
    }
}
