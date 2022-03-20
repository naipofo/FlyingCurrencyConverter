package data.nbpapi.responses


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseRate(
    @SerialName("code")
    val code: String,
    @SerialName("currency")
    val currency: String,
    @SerialName("mid")
    val mid: Double
)