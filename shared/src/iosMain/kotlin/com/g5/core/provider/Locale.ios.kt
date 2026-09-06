package com.g5.core.provider

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun isEnglishLocale(): Boolean =
    (NSLocale.preferredLanguages.firstOrNull() as? String)?.startsWith("en") == true
