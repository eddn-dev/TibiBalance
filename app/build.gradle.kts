@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)     // ← luego Hilt
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.app.tibibalance"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.app.tibibalance"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { compose = true }

    hilt {
        enableAggregatingTask = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeBom.get()
    }

    kotlin { jvmToolchain(17) }
}

dependencies {
    // ── Plataforma Compose ─────────────────────────────────
    implementation(platform(libs.compose.bom))

    // ── Android Core ───────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)

    // ── UI / Compose ───────────────────────────────────────
    implementation(libs.activity.compose)
    implementation(libs.compose.material3)

    // ── Previews ───────────────────────────────────────────
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui.tooling)

    // ── Hilt DI ────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)                     // usa ksp(...) si cambias a KSP

    // ── Unit tests ────────────────────────────────────────
    testImplementation(libs.junit4)

    // ── Instrumented tests ────────────────────────────────
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}