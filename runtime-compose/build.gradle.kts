plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.inject.viewmodel.publish)
    alias(libs.plugins.dependency.analysis)
}

kotlin {
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":runtime"))
                implementation(libs.kotlin.inject.runtime)
                implementation(libs.lifecycle.viewmodel.compose)
            }
        }
    }
}
