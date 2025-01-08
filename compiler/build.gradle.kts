import io.netty.util.internal.PlatformDependent.javaVersion
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.support.kotlinCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.maven.publish)
}

group = "com.teobaranga.kotlin.inject.viewmodel.compiler"

// TODO standardise
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
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
}
