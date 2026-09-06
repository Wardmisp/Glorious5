@file:OptIn(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCacheApi::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

kotlin {
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
            // Supabase Auth pulls in dev.whyoleg.cryptography's CryptoKit cinterop, whose
            // klib doesn't link against the compiler's native cache on this toolchain
            // ("Undefined symbols ... swift_Builtin_float"). Disabling it is the fix the
            // linker error itself points to (https://kotl.in/disable-native-cache).
            disableNativeCache(
                version = org.jetbrains.kotlin.gradle.plugin.mpp.DisableCacheInKotlinVersion.`2_4_10`,
                reason = "Supabase Auth's CryptoKit cinterop klib fails to link against the native cache (Undefined symbols: swift_Builtin_float)"
            )
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.realtime)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.auth)
            implementation(libs.kotlinx.datetime)
            api(libs.androidx.room.runtime)
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
}
