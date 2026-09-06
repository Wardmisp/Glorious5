pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    // Pas de FAIL_ON_PROJECT_REPOS/PREFER_SETTINGS : le plugin Kotlin/Wasm doit pouvoir ajouter
    // lui-même son dépôt de téléchargement de Node.js (https://nodejs.org/dist) pour la cible
    // wasmJs -- PREFER_SETTINGS l'ignore complètement (pas juste "préféré"), ce qui casse ce
    // téléchargement. PREFER_PROJECT (le défaut Gradle) le laisse passer.
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Glorious5"
include(":app")
include(":shared")
include(":webApp")
 