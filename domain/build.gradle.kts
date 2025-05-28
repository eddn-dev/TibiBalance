@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.kotlinx.datetime)

    // ➕ Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.json:json:20231013")  // Última versión disponible hasta 2024

    // Tests
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test) // 👈 ideal para TestScope
}
