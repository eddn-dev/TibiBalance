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
    compileSdk  = 35

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
    /* ── Core & Lifecycle ───────────────────────────── */
    implementation(libs.androidx.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    /* ── Jetpack Compose for Wear OS ────────────────── */
    implementation(platform(libs.compose.bom))            // sigues usando el BOM
    // Wear-specific APIs ↓
    implementation(libs.wear.compose.foundation)          // androidx.wear.compose:compose-foundation:1.4.1
    implementation(libs.wear.compose.material)            // androidx.wear.compose:compose-material:1.4.1
    implementation(libs.wear.compose.navigation)          // androidx.wear.compose:compose-navigation:1.4.1
    debugImplementation(libs.wear.compose.ui.tooling)     // ui-tooling para Wear
    implementation(libs.wear.compose.material3)


    /* ── Health Services ─────────────────────────────── */
    implementation(libs.health.services.client)           // androidx.health:health-services-client:1.0.0

    /* ── Firebase (sin cambios) ─────────────────────── */
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)

    /* ── Hilt ────────────────────────────────────────── */
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)

    /* ── Corrutinas ─────────────────────────────────── */
    implementation(libs.kotlinx.coroutines.core)          // 1.10.2
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.10.2")

    /* ── Wear Data Layer ────────────────────────────── */
    implementation(libs.google.play.services.wearable)
    implementation(libs.health.services.client)   // estable
    implementation(libs.org.jetbrains.kotlinx.kotlinx.coroutines.guava)


    /* ── Serialización JSON ─────────────────────────── */
    implementation(libs.kotlinxSerializationJson)

    /* ── ViewModel Compose ──────────────────────────── */
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    /* ── Tests (sin cambios) ────────────────────────── */
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}

