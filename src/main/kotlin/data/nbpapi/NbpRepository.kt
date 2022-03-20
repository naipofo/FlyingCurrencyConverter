package data.nbpapi.responses

import data.Result
import data.model.CurrencyPricePoint
import data.model.Rate
import data.model.RateResponse

class NbpRepository(
    private val api: NbpApi
) {
    suspend fun getRates(): Result<RateResponse> = try {
        Result.Success(api.getRates()[0].toModel())
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun getGoldPrices(): Result<List<CurrencyPricePoint>> = try {
        Result.Success(api.getGoldPrices().map { it.toModel() })
    } catch (e: Exception) {
        Result.Error(e)
    }
}

private fun GoldPriceResponse.toModel() = CurrencyPricePoint(code = "GOLD", date = data, price = cena)

private fun NbpApiResponse.toModel(): RateResponse = RateResponse(
    date = effectiveDate,
    rates = listOf(
        Rate("PLN", 1.0, "Polski złoty"),
        Rate("PLD", 5.0, "Polski Dolar")
    ) + rates.map { Rate(it.code, it.mid, it.currency) }
)