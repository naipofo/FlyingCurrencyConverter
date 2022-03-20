package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.Result
import data.model.CurrencyPricePoint
import data.nbpapi.responses.NbpRepository
import kotlinx.coroutines.launch
import org.kodein.di.compose.localDI
import org.kodein.di.instance


sealed interface GoldPriceState {
    object Loading : GoldPriceState
    data class Error(val errorMessage: String) : GoldPriceState
    data class Loaded(
        val history: List<CurrencyPricePoint>
    ) : GoldPriceState
}

@Composable
fun GoldPriceRoute() {
    var uiState by remember { mutableStateOf<GoldPriceState>(GoldPriceState.Loading) }
    val scope = rememberCoroutineScope()
    val repository: NbpRepository by localDI().instance()

    fun loadData() = scope.launch {
        uiState = when (val rates = repository.getGoldPrices()) {
            is Result.Error -> GoldPriceState.Error(rates.exception.message ?: "Nieznany error")
            is Result.Success -> GoldPriceState.Loaded(rates.data)
        }
    }

    SideEffect { if (uiState is GoldPriceState.Loading) loadData() }

    when (val state = uiState) {
        is GoldPriceState.Error -> ErrorComponent({ loadData() }, state.errorMessage)
        is GoldPriceState.Loaded -> GoldPriceScreen(state)
        GoldPriceState.Loading -> LoadingComponent()
    }

}

@Composable
fun GoldPriceScreen(
    state: GoldPriceState.Loaded
) {
    Column(Modifier.fillMaxSize()) {
        Text(
            "\uD83C\uDF1F Cena Złota \uD83C\uDF1F",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        CurrencyChart(
            Modifier.fillMaxSize().weight(1f).padding(vertical = 8.dp),
            state.history
        )
    }
}