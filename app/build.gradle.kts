import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dependency.analysis)
    alias(libs.plugins.serialization)
}

group = "com.teobaranga"

kotlin {
    applyDefaultHierarchyTemplate()

    androidLibrary {
        namespace = "com.teobaranga.kotlin.inject.viewmodel.app"
        compileSdk = 36
        minSdk = 24

        withHostTest {
        }

        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KotlinInjectViewModelSample"
            isStatic = true
        }
    }

    jvm(name = "desktop")

    sourceSets {
        getByName("androidHostTest").dependencies {
            implementation(project.dependencies.platform(libs.junit.jupiter.bom))
            implementation(libs.junit.jupiter.core)
            runtimeOnly(libs.junit.jupiter.launcher)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.viewmodel.compose)
            implementation(libs.lifecycle.viewmodel.savedstate)
            implementation(libs.material3)
            implementation(libs.navigation)

            // Code generation
            implementation(libs.kotlin.inject.runtime)
            implementation(libs.kotlin.inject.anvil.runtime)
            implementation(libs.kotlin.inject.anvil.runtime.optional)
            implementation(project(":runtime"))
            implementation(project(":runtime-compose"))
        }
    }

    configureCommonMainKsp()
}

dependencies {
    kspCommonMainMetadata(libs.kotlin.inject.compiler)
    kspCommonMainMetadata(libs.kotlin.inject.anvil.compiler)
    kspCommonMainMetadata(project(":compiler"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

fun KotlinMultiplatformExtension.configureCommonMainKsp() {
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }

    project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
        if (name != "kspCommonMainKotlinMetadata") {
            dependsOn("kspCommonMainKotlinMetadata")
        }
    }
}
