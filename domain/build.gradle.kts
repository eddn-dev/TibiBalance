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

    // Tests
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test) // 👈 ideal para TestScope
}
