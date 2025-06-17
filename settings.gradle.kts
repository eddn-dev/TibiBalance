pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()          // necesario para kotlin.plugin.compose
    }
}

dependencyResolutionManagement {
    // Mantén PREFER_SETTINGS o cámbialo a FAIL_ON_PROJECT_REPOS
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()

        // ► Repos externos necesarios
        maven("https://jitpack.io") {
            name = "jitpack"          // SpinWheelCompose 1.1.1
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots") {
            name = "sonatypeSnapshots"
        }
    }
}


rootProject.name = "TibiBalance"
include(":app")
include(":data")
include(":domain")
