package data.model

data class Rate(
    val code: CurrencyCode,
    val value: Double,
    val fullName: String
)