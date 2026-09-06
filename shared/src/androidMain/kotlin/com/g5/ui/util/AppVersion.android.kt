package com.g5.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun appVersionName(): String {
    val context = LocalContext.current
    return remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.1"
        } catch (_: Exception) {
            "0.0.1"
        }
    }
}
