@file:OptIn(ExperimentalAbiValidation::class)

import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlinJvm)
    `java-gradle-plugin`
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.dokka)
}

mavenPublishing {
    configure(GradlePlugin(
        JavadocJar.Dokka("dokkaGenerateHtml"),
        SourcesJar.Sources(),
    ))
}

gradlePlugin {
    plugins {
        create("kotlinVite") {
            id = "org.drewcarlson.kotlin-vite"
            implementationClass = "org.drewcarlson.kotlinvite.KotlinVitePlugin"
            displayName = "Kotlin Vite"
            description = "Vite support for Kotlin Multiplatform JS targets."
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

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(libs.kotlinGradlePlugin)

    testImplementation(libs.kotlin.test.junit)
    testImplementation(gradleTestKit())
}

System.getenv("GITHUB_REF_NAME")
    ?.takeIf { it.startsWith("v") }
    ?.let { version = it.removePrefix("v") }
