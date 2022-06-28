package data.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.min

fun String.clearNumbers(fallback: String): String = try {
    replace(",", ".").slice(0..min(9, length - 1)).toDouble().roundCurrency()
} catch (_: Exception) {
    fallback
}

fun Double.roundCurrency(): String = DecimalFormat("#0.00", DecimalFormatSymbols(Locale.US)).format(this)
