// wear/build.gradle.kts
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Google Services para procesar google-services.json
    alias(libs.plugins.google.services)

    // Hilt
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace   = "com.app.wear"
    compileSdk  = 36

    defaultConfig {
        applicationId    = "com.app.wear"
        minSdk           = 30
        targetSdk        = 35
        versionCode      = 1
        versionName      = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        // Ajusta al versión de tu Compose Compiler
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

dependencies {
    implementation(platform(libs.compose.bom))

    /* Core + lifecycle */
    implementation(libs.androidx.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    /* Compose Wear */
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.navigation)
    implementation(libs.wear.compose.material3)
    debugImplementation(libs.wear.compose.ui.tooling)

    /* Health Services 1.1.0 */
    implementation(libs.health.services.client)
    implementation(libs.androidx.activity.ktx)
    /* Data Layer */
    implementation(libs.google.play.services.wearable)

    /* Coroutines */
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.guava)

    /* kotlinx-datetime & serialization */
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinxSerializationJson)

    /* Hilt */
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)

    /* ── Tests (sin cambios) ────────────────────────── */
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)


    implementation("io.github.pseudoankit:coachmark:3.0.1")
}

