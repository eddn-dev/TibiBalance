@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)    // Firebase Gradle Plugin
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.ksp)         // Hilt & Room via KSP
}

android {
    namespace   = "com.app.tibibalance"
    compileSdk  = 35

    defaultConfig {
        applicationId         = "com.app.tibibalance"
        minSdk                = 24
        targetSdk             = 35
        versionCode           = 1
        versionName           = "0.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures { compose = true }

    // Java 17 toolchain
    kotlin { jvmToolchain(17) }

}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    // Inyeccion de dependencias internas de la aplicacion
    implementation(project(":domain"))
    implementation(project(":data"))

    /* ── Compose BOM ─────────────────────────────────── */
    implementation(platform(libs.compose.bom))

    /* ── Core / KotlinX ──────────────────────────────── */
    implementation(libs.androidx.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinxSerializationJson)

    /* ── UI (Compose) ───────────────────────────────── */
    implementation(libs.bundles.compose)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.material.icons.extended)

    /* ── Media / Animations ─────────────────────────── */
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.lottie.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.spinwheel.compose)
    implementation(libs.tap.target.compose)

    /* ── Room (local DB) ────────────────────────────── */
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    /* ── Hilt DI ────────────────────────────────────── */
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation)
    implementation(libs.navigation.compose)

    /* ── Firebase (plataforma BoM) ──────────────────── */
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.analytics.ktx)

    /* ── Google Identity / Credential Manager ───────── */
    implementation(libs.bundles.auth)

    /* ── Coroutines Play Svc helpers ────────────────── */
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.collections.immutable)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation(libs.json)  // Última versión disponible hasta 2024

    /* ── Coroutines ─────────────────────────────────── */
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)

    implementation("io.github.pseudoankit:coachmark:3.0.1")

    /* ── Tests ──────────────────────────────────────── */
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
