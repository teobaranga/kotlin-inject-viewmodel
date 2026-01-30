plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

group = "com.teobaranga"

android {
    namespace = "com.teobaranga.kotlin.inject.viewmodel"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.teobaranga.kotlin.inject.viewmodel"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)

    implementation(project(":app"))
}
