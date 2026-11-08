package com.estcomputer.patient.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class VerifyForbiddenDependenciesTask : DefaultTask() {

    @get:Internal
    abstract val classpathConfigurations: ListProperty<Configuration>

    @TaskAction
    fun verify() {
        val coordinates = classpathConfigurations.get()
            .flatMap { config ->
                config.incoming.resolutionResult.allComponents.mapNotNull { component ->
                    (component.id as? ModuleComponentIdentifier)?.let { "${it.group}:${it.module}" }
                }
            }
            .distinct()
        val violations = ForbiddenDependencyRules.violations(coordinates)
        if (violations.isNotEmpty()) {
            throw GradleException("Forbidden dependencies in ${project.path}: ${violations.joinToString()}")
        }
    }
}
