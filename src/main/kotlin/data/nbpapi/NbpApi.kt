package data.nbpapi.responses

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class NbpApi(
    private val client: HttpClient
) {
    suspend fun getRates(): List<NbpApiResponse> =
        client.get("https://api.nbp.pl/api/exchangerates/tables/A?format=json").body()
}