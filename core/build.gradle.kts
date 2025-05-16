@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)   // org.jetbrains.kotlin.jvm 2.0.21
}

kotlin { jvmToolchain(17) }          // genera byte-code y std-lib para Java 17

dependencies {
    // solo utilidades de prueba – la librería es JVM pura
    testImplementation(libs.junit4)
}
