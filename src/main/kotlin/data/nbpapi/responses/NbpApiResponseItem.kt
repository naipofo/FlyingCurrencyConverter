package data.nbpapi.responses


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NbpApiResponse(
    @SerialName("effectiveDate")
    val effectiveDate: String,
    @SerialName("no")
    val no: String,
    @SerialName("rates")
    val rates: List<ResponseRate>,
    @SerialName("table")
    val table: String
)