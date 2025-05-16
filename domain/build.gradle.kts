@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":core"))   // reutiliza helpers comunes
    implementation(libs.kotlinxSerializationJson)
    implementation(libs.kotlinx.datetime)
    testImplementation(libs.junit4)
}
