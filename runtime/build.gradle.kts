plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
}

kotlin {
    androidTarget()

    jvm()

    applyDefaultHierarchyTemplate()
}

android {
    namespace = "com.teobaranga.kotlin.inject.viewmodel.runtime"
    compileSdk = 35
}

dependencies {
    commonMainImplementation(libs.kotlin.inject.runtime)
    commonMainImplementation(libs.kotlin.inject.anvil.runtime)
    commonMainImplementation(libs.lifecycle.viewmodel)
    commonMainImplementation(libs.lifecycle.viewmodel.savedstate)
}
