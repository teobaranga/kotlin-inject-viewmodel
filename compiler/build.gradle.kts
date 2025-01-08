plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.ksp)
}

group = "com.teobaranga.kotlininject.viewmodel.compiler"

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
