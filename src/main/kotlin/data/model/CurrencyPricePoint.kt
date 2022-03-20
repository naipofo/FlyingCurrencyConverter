package data.model

data class CurrencyPricePoint(
    val code: CurrencyCode,
    val date: String,
    val price: Double,
)