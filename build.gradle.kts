@file:OptIn(ExperimentalAbiValidation::class)

import com.vanniktech.maven.publish.GradlePublishPlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.dokka)
}

mavenPublishing {
    configure(GradlePublishPlugin())
}

gradlePlugin {
    website.set("https://github.com/DrewCarlson/kotlin-vite")
    vcsUrl.set("https://github.com/DrewCarlson/kotlin-vite.git")
    plugins {
        create("kotlinVite") {
            id = "org.drewcarlson.kotlin-vite"
            implementationClass = "org.drewcarlson.kotlinvite.KotlinVitePlugin"
            displayName = "Kotlin Vite"
            description = "Vite support for Kotlin Multiplatform JS targets."
            tags.set(listOf("kotlin", "vite", "kotlin-multiplatform", "kotlin-js"))
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(21)
    explicitApi()
    compilerOptions {
        progressiveMode.set(false)
        apiVersion.set(KotlinVersion.KOTLIN_2_0)
        languageVersion.set(KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(JvmTarget.JVM_17)
    }
    abiValidation {
        enabled.set(true)
    }
}

val testPluginClasspath: Configuration by configurations.creating {
    extendsFrom(configurations.compileOnly.get())
}

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(libs.kotlinGradlePlugin)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(gradleTestKit())
}

tasks.pluginUnderTestMetadata {
    pluginClasspath.from(testPluginClasspath)
}

System.getenv("GITHUB_REF_NAME")
    ?.takeIf { it.startsWith("v") }
    ?.let { version = it.removePrefix("v") }
