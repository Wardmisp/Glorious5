package com.g5.core.provider

import java.util.Locale

actual fun isEnglishLocale(): Boolean = Locale.getDefault().language == "en"
