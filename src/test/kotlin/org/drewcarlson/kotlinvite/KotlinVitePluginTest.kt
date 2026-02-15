package org.drewcarlson.kotlinvite

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertTrue

class KotlinVitePluginTest {

    @get:Rule
    val testProjectDir = TemporaryFolder()

    @Test
    fun `plugin can be applied with KMP plugin`() {
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            rootProject.name = "test-project"
            """.trimIndent(),
        )
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                kotlin("multiplatform") version "2.2.20"
                id("org.drewcarlson.kotlin-vite")
            }

            kotlin {
                js(IR) {
                    browser()
                    binaries.executable()
                }
            }
            """.trimIndent(),
        )
        val srcDir = File(testProjectDir.root, "src/jsMain/kotlin")
        srcDir.mkdirs()
        File(srcDir, "Main.kt").writeText("fun main() {}")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments("tasks", "--all")
            .build()

        assertTrue(
            result.output.contains("jsBrowserDevelopmentServe") || result.output.contains("jsBrowserProductionServe"),
            "Expected Vite tasks in output but got:\n${result.output}",
        )
    }

    @Test
    fun `plugin can be disabled`() {
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            rootProject.name = "test-project"
            """.trimIndent(),
        )
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                kotlin("multiplatform") version "2.2.20"
                id("org.drewcarlson.kotlin-vite")
            }

            kotlinVite {
                enabled.set(false)
            }

            kotlin {
                js(IR) {
                    browser()
                    binaries.executable()
                }
            }
            """.trimIndent(),
        )
        val srcDir = File(testProjectDir.root, "src/jsMain/kotlin")
        srcDir.mkdirs()
        File(srcDir, "Main.kt").writeText("fun main() {}")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments("tasks", "--all")
            .build()

        assertTrue(
            !result.output.contains("jsBrowserDevelopmentServe"),
            "Expected no Vite tasks when disabled but found them in output",
        )
    }

    @Test
    fun `disableWebpack disables webpack tasks by default`() {
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            rootProject.name = "test-project"
            """.trimIndent(),
        )
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                kotlin("multiplatform") version "2.2.20"
                id("org.drewcarlson.kotlin-vite")
            }

            kotlin {
                js(IR) {
                    browser()
                    binaries.executable()
                }
            }

            tasks.register("checkWebpackEnabled") {
                doLast {
                    listOf(
                        "jsBrowserDevelopmentWebpack",
                        "jsBrowserProductionWebpack",
                        "jsBrowserDevelopmentRun",
                        "jsBrowserProductionRun",
                        "jsBrowserDistribution",
                    ).forEach { name ->
                        val task = tasks.findByName(name)
                        println("${'$'}name.enabled=${'$'}{task?.enabled}")
                    }
                }
            }
            """.trimIndent(),
        )
        val srcDir = File(testProjectDir.root, "src/jsMain/kotlin")
        srcDir.mkdirs()
        File(srcDir, "Main.kt").writeText("fun main() {}")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments("checkWebpackEnabled")
            .build()

        val webpackTasks = listOf(
            "jsBrowserDevelopmentWebpack",
            "jsBrowserProductionWebpack",
            "jsBrowserDevelopmentRun",
            "jsBrowserProductionRun",
            "jsBrowserDistribution",
        )
        for (taskName in webpackTasks) {
            assertTrue(
                result.output.contains("$taskName.enabled=false"),
                "Expected '$taskName' to be disabled but output was:\n${result.output}",
            )
        }
    }

    @Test
    fun `disableWebpack false preserves webpack tasks`() {
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            rootProject.name = "test-project"
            """.trimIndent(),
        )
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                kotlin("multiplatform") version "2.2.20"
                id("org.drewcarlson.kotlin-vite")
            }

            kotlinVite {
                disableWebpack = false
            }

            kotlin {
                js(IR) {
                    browser()
                    binaries.executable()
                }
            }

            tasks.register("checkWebpackEnabled") {
                doLast {
                    listOf(
                        "jsBrowserDevelopmentWebpack",
                        "jsBrowserProductionWebpack",
                        "jsBrowserDevelopmentRun",
                        "jsBrowserProductionRun",
                        "jsBrowserDistribution",
                    ).forEach { name ->
                        val task = tasks.findByName(name)
                        println("${'$'}name.enabled=${'$'}{task?.enabled}")
                    }
                }
            }
            """.trimIndent(),
        )
        val srcDir = File(testProjectDir.root, "src/jsMain/kotlin")
        srcDir.mkdirs()
        File(srcDir, "Main.kt").writeText("fun main() {}")

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withPluginClasspath()
            .withArguments("checkWebpackEnabled")
            .build()

        val webpackTasks = listOf(
            "jsBrowserDevelopmentWebpack",
            "jsBrowserProductionWebpack",
            "jsBrowserDevelopmentRun",
            "jsBrowserProductionRun",
            "jsBrowserDistribution",
        )
        for (taskName in webpackTasks) {
            assertTrue(
                result.output.contains("$taskName.enabled=true"),
                "Expected '$taskName' to be enabled but output was:\n${result.output}",
            )
        }
    }
}
