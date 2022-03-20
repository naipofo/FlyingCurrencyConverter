package data.nbpapi.responses


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoldPriceResponse(
    @SerialName("cena")
    val cena: Double,
    @SerialName("data")
    val `data`: String
)