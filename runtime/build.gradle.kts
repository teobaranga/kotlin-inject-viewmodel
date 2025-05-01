plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.inject.viewmodel.publish)
    alias(libs.plugins.dependency.analysis)
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.inject.runtime)
                implementation(libs.kotlin.inject.anvil.runtime)
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.viewmodel.savedstate)
            }
        }
    }
}
