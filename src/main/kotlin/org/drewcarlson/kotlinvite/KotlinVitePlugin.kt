package org.drewcarlson.kotlinvite

import org.drewcarlson.kotlinvite.tasks.BaseViteTask
import org.drewcarlson.kotlinvite.tasks.ViteDistTask
import org.drewcarlson.kotlinvite.tasks.ViteServeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsBinaryMode
import org.jetbrains.kotlin.gradle.targets.js.ir.JsIrBinary
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrCompilation
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.npm.npmProject

public class KotlinVitePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            "kotlinVite",
            KotlinViteExtension::class.java,
        )

        project.afterEvaluate {
            if (!extension.enabled.get()) {
                return@afterEvaluate
            }

            val targets = findJsTargets(project)
            check(targets.isNotEmpty()) {
                "kotlin-vite requires at least one Kotlin/JS browser target. " +
                    "Configure a js(IR) { browser() } target in your Kotlin Multiplatform configuration."
            }

            extension.configureViteTasks(project, targets)
        }
    }

    private fun findJsTargets(project: Project): List<KotlinJsIrTarget> {
        val kmp = checkNotNull(project.extensions.findByType(KotlinMultiplatformExtension::class.java)) {
            "kotlin-vite requires the Kotlin Multiplatform Gradle plugin to be applied."
        }
        return kmp.targets.filterIsInstance<KotlinJsIrTarget>()
    }

    private fun KotlinViteExtension.configureViteTasks(project: Project, targets: List<KotlinJsIrTarget>) {
        targets.forEach { target ->
            val mainCompilation = target.compilations.getByName("main")
            if (addViteDependency) {
                mainCompilation.dependencies {
                    implementation(devNpm("vite", viteVersion))
                }
            }
            mainCompilation.binaries.executable().forEach { binary ->
                if (target.isBrowserConfigured) {
                    configureViteTask<ViteServeTask>(project, target.name, mainCompilation, binary)
                    configureViteTask<ViteDistTask>(project, target.name, mainCompilation, binary)
                }
            }
        }

        if (disableWebpack) {
            targets.forEach { target ->
                val taskNames = listOf(
                    "${target.name}BrowserDevelopmentWebpack",
                    "${target.name}BrowserProductionWebpack",
                    "${target.name}BrowserDevelopmentRun",
                    "${target.name}BrowserProductionRun",
                    "${target.name}BrowserDistribution",
                )
                taskNames.forEach { name ->
                    project.tasks.matching { it.name == name }.configureEach { it.enabled = false }
                }
            }
        }
    }

    private inline fun <reified T : BaseViteTask> KotlinViteExtension.configureViteTask(
        project: Project,
        targetName: String,
        compilation: KotlinJsIrCompilation,
        binary: JsIrBinary,
    ) {
        val mode = when (binary.mode) {
            KotlinJsBinaryMode.DEVELOPMENT -> "Development"
            KotlinJsBinaryMode.PRODUCTION -> "Production"
        }
        val taskNameSuffix = if (T::class == ViteServeTask::class) "Serve" else "Dist"
        project.tasks.register("${targetName}Browser${mode}${taskNameSuffix}", T::class.java) { task ->
            task.description = "Build '${project.path}:${targetName}' with Vite."

            val npm = compilation.npmProject
            task.workingDir.set(npm.dist)
            task.viteScript.set(npm.nodeJsRoot.rootPackageDirectory.map { it.file("node_modules/vite/bin/vite.js") })
            task.nodeExecutable.set(npm.nodeJs.executable)
            task.configFile.set(configFilePath.get())
            environment["NODE_PATH"] = npm.nodeJsRoot.rootPackageDirectory.get().asFile.absolutePath + "/node_modules"
            task.env.set(environment)
            if (task is ViteDistTask) {
                task.outputDir.set(project.layout.buildDirectory.dir("vite/${targetName}/${binary.name}"))
            }

            task.dependsOn(
                npm.nodeJsRoot.npmInstallTaskProvider,
                binary.linkSyncTask,
            )
        }
    }
}
