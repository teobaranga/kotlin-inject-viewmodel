plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.inject.viewmodel.publish)
}

kotlin {
    androidTarget()
    jvm()
}

android {
    namespace = "com.teobaranga.kotlin.inject.viewmodel.runtime.compose"
    compileSdk = 35
}

dependencies {
    commonMainImplementation(project(":runtime"))
    commonMainImplementation(libs.kotlin.inject.runtime)
    commonMainImplementation(libs.lifecycle.viewmodel.compose)
}
