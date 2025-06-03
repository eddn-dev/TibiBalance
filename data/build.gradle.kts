@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.ksp)

}

android {
    namespace  = "com.app.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    kotlin { jvmToolchain(17) }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")   // ← habilita value-classes
}


dependencies {
    /* ─── Flecha Clean Arch ─────────────────────────────────────────────── */
    implementation(project(":domain"))
    implementation(project(":core"))

    /* ─── Inyección de dependencias ────────────────────────────────────── */
    implementation(libs.hilt.android)
    implementation(libs.google.play.services.wearable)
    ksp(libs.hilt.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    /* ─── Room + SQLCipher (caché local cifrada) ───────────────────────── */
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.android.database.sqlcipher)

    /* ─── Firestore remoto ─────────────────────────────────────────────── */
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")

    /* ─── Autenticación Firebase ───────────────────────────────────────── */
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.play.services.auth)

    /* ─── KotlinX Date & Time (Instant, LocalDate…) ────────────────────── */
    implementation(libs.kotlinx.datetime)

    /* ─── KotlinX Serialization JSON runtime ───────────────────────────── */
    implementation(libs.kotlinxSerializationJson)

    /* ─── Utilidades AndroidX mínimas ──────────────────────────────────── */
    implementation(libs.androidx.core.ktx)

    /* ─── Tests ────────────────────────────────────────────────────────── */
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    // data/build.gradle.kts
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // EncryptedSharedPreferences :contentReference[oaicite:0]{index=0}

    /* ─── Coroutines ─────────────────────────────── */
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)

    /* ─── WorkManager (para SyncWorker) ───────────── */
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)

    // Librería de JSON para kotlinx.serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

}
