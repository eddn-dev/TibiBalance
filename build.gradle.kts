@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library)     apply false
    alias(libs.plugins.kotlin.android)      apply false
    alias(libs.plugins.kotlin.compose)      apply false
    alias(libs.plugins.kotlin.kapt)         apply false   // ✔︎ alias existe
    alias(libs.plugins.kotlin.jvm)          apply false   // ✔︎ alias existe
    alias(libs.plugins.hilt.android)        apply false
    alias(libs.plugins.dokka)               apply false
}


tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
