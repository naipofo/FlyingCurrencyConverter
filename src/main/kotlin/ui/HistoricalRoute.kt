package ui

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import data.Result
import data.model.CurrencyPricePoint
import data.model.Rate
import data.nbpapi.responses.NbpRepository
import data.unpackOr
import kotlinx.coroutines.launch
import org.kodein.di.compose.localDI
import org.kodein.di.instance

data class RawHistoricalState(
    val availableCurrencies: List<Rate>? = null,
    val errorMessage: String? = null,
    val fromCur: Rate? = null,
    val toCur: Rate? = null,
    val fromData: List<CurrencyPricePoint>? = null,
    val toData: List<CurrencyPricePoint>? = null
) {
    fun toUiState(): HistoricalState =
        if (availableCurrencies != null && fromCur != null && toCur != null && toData != null && fromData != null) {
            HistoricalState.Loaded(availableCurrencies, fromCur, toCur, fromData, toData)
        } else if (errorMessage != null) {
            HistoricalState.Error(errorMessage)
        } else HistoricalState.Loading
}

sealed interface HistoricalState {
    object Loading : HistoricalState
    data class Error(val errorMessage: String) : HistoricalState
    data class Loaded(
        val availableCurrencies: List<Rate>,
        val fromCur: Rate,
        val toCur: Rate,
        val fromData: List<CurrencyPricePoint>,
        val toData: List<CurrencyPricePoint>
    ) : HistoricalState
}

@Composable
fun HistoricalRoute() {
    var rawState by remember { mutableStateOf(RawHistoricalState()) }
    val scope = rememberCoroutineScope()
    val repository: NbpRepository by localDI().instance()

    val uiState = rawState.toUiState()

    fun loadHistoricalRates() = scope.launch {
        val state = rawState
        if (state.fromCur != null && state.toCur != null) rawState = rawState.copy(
            fromData = repository.getHistoricalPricing(state.fromCur.code).unpackOr {
                rawState = rawState.copy(errorMessage = it.message)
                return@launch
            },
            toData = repository.getHistoricalPricing(state.toCur.code).unpackOr {
                rawState = rawState.copy(errorMessage = it.message)
                return@launch
            },
        )
    }


    fun loadInitial() = scope.launch {
        rawState = when (val rates = repository.getRates()) {
            is Result.Error -> rawState.copy(errorMessage = rates.exception.message ?: "unknown error")
            is Result.Success -> rawState.copy(
                availableCurrencies = rates.data.rates,
                fromCur = rates.data.rates[0],
                toCur = rates.data.rates[1]
            ).also { loadHistoricalRates() }
        }
    }

    SideEffect { if (rawState.availableCurrencies == null) loadInitial() }

    when (uiState) {
        is HistoricalState.Error -> ErrorComponent({ loadInitial() }, uiState.errorMessage)
        is HistoricalState.Loaded -> HistoricalScreen(uiState)
        HistoricalState.Loading -> LoadingComponent()
    }
}

@Composable
fun HistoricalScreen(state: HistoricalState.Loaded) {
    Text("Przelicznik ${state.fromCur.code} na ${state.toCur.code}")
    // TODO: graph
}