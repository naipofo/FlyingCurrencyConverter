package data.util

import kotlin.math.min
import kotlin.math.roundToInt

fun String.clearNumbers(fallback: String): String = try {
    replace(",", ".").slice(0..min(9, length - 1)).toDouble().roundCurrency().toString()
} catch (_: Exception) {
    fallback
}

fun Double.roundCurrency(): Double = (this * 100).roundToInt().toDouble() / 100