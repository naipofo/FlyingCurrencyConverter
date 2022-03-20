package data.nbpapi.responses

import data.Result
import data.model.CurrencyCode
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

    suspend fun getHistoricalPricing(code: CurrencyCode): Result<List<CurrencyPricePoint>> = if (code.value == "PLN") {
        Result.Success(listOf(CurrencyPricePoint(CurrencyCode("PLN"), "2000-02-02", 1.0)))
    } else try {
        Result.Success(api.getHistoricalPricing(code.value.lowercase()).rates.map { it.toModel(code) })
    } catch (e: Exception) {
        Result.Error(e)
    }
}

private fun ApiHisRate.toModel(code: CurrencyCode) =
    CurrencyPricePoint(code = code, date = effectiveDate, price = mid)

private fun GoldPriceResponse.toModel() =
    CurrencyPricePoint(code = CurrencyCode("GOLD"), date = data, price = cena)

private fun NbpApiResponse.toModel(): RateResponse = RateResponse(
    date = effectiveDate,
    rates = listOf(
        Rate(CurrencyCode("PLN"), 1.0, "Polski złoty"),
    ) + rates.map { Rate(CurrencyCode(it.code), it.mid, it.currency) }
)