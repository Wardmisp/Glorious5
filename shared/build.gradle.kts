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

    // Supabase Auth pulls in dev.whyoleg.cryptography's CryptoKit provider for iOS, whose Swift
    // interop needs Apple's Swift standard libraries explicitly on the linker path -- otherwise
    // "Undefined symbols ... swift_Builtin_float". The dev.whyoleg.cryptography Gradle plugin is
    // supposed to add this automatically (configureSwiftLinkerOpts) but didn't in practice, so
    // it's resolved by hand here via `xcode-select`, no hardcoded Xcode version/path.
    val appleTargetPlatforms = mapOf(
        iosArm64() to "iphoneos",
        iosSimulatorArm64() to "iphonesimulator"
    )

    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX) {
        val developerDir = providers.exec {
            commandLine("xcode-select", "-p")
        }.standardOutput.asText.get().trim()

        appleTargetPlatforms.forEach { (target, platformDir) ->
            target.binaries.configureEach {
                linkerOpts("-L$developerDir/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/$platformDir")
            }
        }
    }

    appleTargetPlatforms.keys.forEach {
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
