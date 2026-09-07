package com.g5.core.provider

import kotlinx.browser.window

actual fun isEnglishLocale(): Boolean = window.navigator.language.startsWith("en")
