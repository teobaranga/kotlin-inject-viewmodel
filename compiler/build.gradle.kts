import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.inject.viewmodel.publish)
}

group = "com.teobaranga.kotlin.inject.viewmodel.compiler"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

dependencies {
    // Regular dependencies
    implementation(project(":runtime"))
    implementation(libs.kotlin.inject.runtime)
    implementation(libs.kotlin.inject.anvil.runtime)
    implementation(libs.kotlin.inject.anvil.runtime.optional)
    implementation(libs.ksp.api)

    // Generate code
    implementation(libs.kotlin.poet)
    implementation(libs.kotlin.poet.ksp)

    // Register KSP providers
    implementation(libs.auto.service.annotations)
    ksp(libs.auto.service.ksp)

    testImplementation(platform(libs.junit.jupiter.bom))
    testImplementation(libs.junit.jupiter.core)
    testRuntimeOnly(libs.junit.jupiter.launcher)

    testImplementation(libs.kotlin.compile.testing.core)
    testImplementation(libs.kotlin.compile.testing.ksp)

    testImplementation(libs.kotlin.reflect)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}
