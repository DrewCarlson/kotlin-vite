package org.drewcarlson.kotlinvite

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ViteFunctionalTest {

    @get:Rule
    val testProjectDir = TemporaryFolder()

    private val defaultViteConfig = """
        import { defineConfig } from 'vite';
        export default defineConfig({});
    """.trimIndent()

    private fun setupProject(
        extraBuildConfig: String = "",
        viteConfigContent: String = defaultViteConfig,
    ) {
        testProjectDir.newFile("settings.gradle.kts").writeText(
            """
            rootProject.name = "kotlin-vite-test"
            """.trimIndent(),
        )
        testProjectDir.newFile("build.gradle.kts").writeText(
            """
            plugins {
                kotlin("multiplatform") version "2.2.20"
                id("org.drewcarlson.kotlin-vite")
            }

            repositories {
                mavenCentral()
            }

            kotlin {
                js(IR) {
                    browser()
                    binaries.executable()
                }
            }
            $extraBuildConfig
            """.trimIndent(),
        )

        val srcDir = File(testProjectDir.root, "src/jsMain/kotlin")
        srcDir.mkdirs()
        File(srcDir, "Main.kt").writeText("""fun main() { println("hello") }""")

        val resourceDir = File(testProjectDir.root, "src/jsMain/resources")
        resourceDir.mkdirs()
        File(resourceDir, "index.html").writeText(
            """
            <!DOCTYPE html>
            <html>
            <head><title>Test</title></head>
            <body><script src="kotlin-vite-test.js"></script></body>
            </html>
            """.trimIndent(),
        )

        testProjectDir.newFile("vite.config.js").writeText(viteConfigContent)
    }

    private fun runner(vararg args: String) = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withPluginClasspath()
        .withArguments(*args, "--stacktrace")
        .forwardOutput()

    @Test
    fun `viteDist production build produces output`() {
        setupProject()

        val result = runner("jsBrowserProductionDist").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":jsBrowserProductionDist")?.outcome)
        val outputDir = File(testProjectDir.root, "build/vite/js/productionExecutable")
        assertTrue(outputDir.exists(), "Expected output directory to exist: $outputDir")
        val files = outputDir.walkTopDown().filter { it.isFile }.toList()
        assertTrue(files.isNotEmpty(), "Expected output directory to contain files")
    }

    @Test
    fun `viteDist development build produces output`() {
        setupProject()

        val result = runner("jsBrowserDevelopmentDist").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":jsBrowserDevelopmentDist")?.outcome)
        val outputDir = File(testProjectDir.root, "build/vite/js/developmentExecutable")
        assertTrue(outputDir.exists(), "Expected output directory to exist: $outputDir")
        val files = outputDir.walkTopDown().filter { it.isFile }.toList()
        assertTrue(files.isNotEmpty(), "Expected output directory to contain files")
    }

    @Test
    fun `viteDist with custom vite config`() {
        setupProject(
            viteConfigContent = """
                import { defineConfig } from 'vite';
                export default defineConfig({
                    build: {
                        outDir: 'custom-output',
                    },
                });
            """.trimIndent(),
        )

        val result = runner("jsBrowserProductionDist").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":jsBrowserProductionDist")?.outcome)
    }
}
