@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    // Configurer manuellement le dependsOn de roomMain plus bas désactive sinon l'application
    // automatique du modèle de hiérarchie par défaut (qui crée entre autres iosMain à partir de
    // iosArm64()/iosSimulatorArm64()) -- il faut le réappliquer explicitement.
    applyDefaultHierarchyTemplate()

    android {
        namespace = "com.g5.shared"
        compileSdk = 37
        minSdk = 28
        androidResources {
            enable = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    wasmJs {
        browser()
    }

    sourceSets {
        // Room (androidx.room, 2.8.4) n'a pas de variante wasmJs -- ses annotations doivent
        // rester hors de commonMain pour ne pas casser la compilation web (voir le plan de
        // portage web : le web n'utilise pas Room du tout, juste un export JSON en mémoire).
        // Android et iOS partagent donc ce sous-ensemble intermédiaire plutôt que commonMain.
        val roomMain = create("roomMain") {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(roomMain)
        iosMain.get().dependsOn(roomMain)

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.realtime)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.kotlinx.datetime)

            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.materialIconsExtended)
            api(compose.ui)
            implementation(compose.components.resources)

            api(libs.koin.core)
            api(libs.koin.core.viewmodel)
            api(libs.koin.compose.viewmodel)
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.androidx.navigation.compose)
        }
        roomMain.dependencies {
            api(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.kotlinx.browser)
        }
    }
}

compose.resources {
    packageOfResClass = "com.g5.shared.resources"
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}
