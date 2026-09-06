package com.g5.ui.util

import kotlin.math.abs
import kotlin.math.round

/** Équivalent multiplateforme de `"%.1f".format(this)` (`String.format`/`java.util.Formatter`
 * sont JVM-only, indisponibles sur iOS). */
fun Double.formatOneDecimal(): String {
    val rounded = round(this * 10) / 10
    val whole = rounded.toInt()
    val decimal = abs(round((rounded - whole) * 10).toInt())
    return "$whole.$decimal"
}
