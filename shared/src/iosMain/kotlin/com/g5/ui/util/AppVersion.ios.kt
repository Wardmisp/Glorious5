package com.g5.ui.util

import androidx.compose.runtime.Composable
import platform.Foundation.NSBundle

@Composable
actual fun appVersionName(): String =
    (NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String) ?: "0.0.1"
