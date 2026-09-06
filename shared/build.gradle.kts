plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.cryptography.kotlin)
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

// Supabase Auth pulls in dev.whyoleg.cryptography's CryptoKit provider for iOS, which hardcodes
// a path to /Applications/Xcode.app when linking against the platform's Swift standard libraries
// -- missing on CI runners where Xcode is only installed as e.g. Xcode_15.4.app. This plugin
// resolves the real path via `xcrun` instead. See:
// https://whyoleg.github.io/cryptography-kotlin/getting-started/troubleshooting/xcode-compatibility/
cryptography {
    configureSwiftLinkerOpts = true
}
