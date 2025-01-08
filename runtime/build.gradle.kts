plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.compose.compiler)
}

group = "com.teobaranga.kotlininject.viewmodel.runtime"

dependencies {
    implementation(libs.kotlin.inject.runtime)
    implementation(libs.kotlin.inject.anvil.runtime)
    implementation(libs.lifecycle.viewmodel.compose)
}
