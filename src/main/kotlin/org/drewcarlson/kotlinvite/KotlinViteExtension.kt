package org.drewcarlson.kotlinvite

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public abstract class KotlinViteExtension @Inject constructor(
    project: Project,
    objects: ObjectFactory,
) {
    /**
     * Enable or disable all kotlin-vite functionality and tasks.
     */
    public var enabled: Property<Boolean> = objects.property(Boolean::class.java).convention(true)

    /**
     * Vite version to use if auto installed (enabled by default).
     */
    public var viteVersion: String = "6.4.1"

    /**
     * Enable or disable automatic Vite dependency handling, enabled by default.
     */
    public var addViteDependency: Boolean = true

    /**
     * Disable redundant webpack, run, and distribution tasks when using Vite, enabled by default.
     */
    public var disableWebpack: Boolean = true

    /**
     * Custom environment variables to pass to Vite dev server.
     */
    public var environment: MutableMap<String, String> = mutableMapOf()

    /**
     * Optional path to a vite.config.js file in the project.
     * Defaults to projectDir/vite.config.js if present.
     */
    public var configFilePath: RegularFileProperty = objects.fileProperty()
        .convention { project.file("vite.config.js") }
}
