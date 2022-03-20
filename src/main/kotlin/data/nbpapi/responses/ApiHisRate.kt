package data.nbpapi.responses


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiHisRate(
    @SerialName("effectiveDate")
    val effectiveDate: String,
    @SerialName("mid")
    val mid: Double,
    @SerialName("no")
    val no: String
)