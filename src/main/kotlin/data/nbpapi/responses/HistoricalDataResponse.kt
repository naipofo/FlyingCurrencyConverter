package data.nbpapi.responses


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoricalDataResponse(
    @SerialName("code")
    val code: String,
    @SerialName("currency")
    val currency: String,
    @SerialName("rates")
    val rates: List<ApiHisRate>,
    @SerialName("table")
    val table: String
)