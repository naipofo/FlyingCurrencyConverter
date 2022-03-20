package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    val toData: List<CurrencyPricePoint>? = null,
    val isCurrencyDialogOpen: Boolean = false,
    val currentlyChanging: CurrentlyChanging = CurrentlyChanging.FROM
) {
    fun toUiState(): HistoricalState =
        if (availableCurrencies != null && fromCur != null && toCur != null && toData != null && fromData != null) {
            HistoricalState.Loaded(availableCurrencies, fromCur, toCur, fromData, toData, isCurrencyDialogOpen)
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
        val toData: List<CurrencyPricePoint>,
        val isCurrencyDialogOpen: Boolean
    ) : HistoricalState
}

enum class CurrentlyChanging {
    FROM, TO
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
        is HistoricalState.Loaded -> HistoricalScreen(
            state = uiState,
            onFromClick = {
                rawState = rawState.copy(
                    isCurrencyDialogOpen = true,
                    currentlyChanging = CurrentlyChanging.FROM
                )
            },
            onToClick = {
                rawState = rawState.copy(
                    isCurrencyDialogOpen = true,
                    currentlyChanging = CurrentlyChanging.TO
                )
            },
            onSwapClick = {
                rawState = rawState.copy(
                    toCur = rawState.fromCur,
                    fromCur = rawState.toCur,
                    toData = rawState.fromData,
                    fromData = rawState.toData
                )
            },
            onSelectElementFromDialog = {
                rawState = rawState.copy(isCurrencyDialogOpen = false)
                rawState = when (rawState.currentlyChanging) {
                    CurrentlyChanging.FROM -> rawState.copy(fromCur = it)
                    CurrentlyChanging.TO -> rawState.copy(toCur = it)
                }
                loadHistoricalRates()
            }
        )
        HistoricalState.Loading -> LoadingComponent()
    }
}

@Composable
fun HistoricalScreen(
    state: HistoricalState.Loaded,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onSwapClick: () -> Unit,
    onSelectElementFromDialog: (Rate) -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                @Composable
                fun RowScope.indc(text: String, ev: () -> Unit) = Text(
                    text,
                    Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { ev() }
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
                indc(state.fromCur.code.value) { onFromClick() }
                IconButton({ onSwapClick() }) {
                    Icon(Icons.Default.SwapHoriz, "Swap")
                }
                indc(state.toCur.code.value) { onToClick() }
            }
            CompareCurrencyGraph(state.fromData, state.toData)
            Text(
                "2020-02-02\n1.00 PLN - 3.4 EUR",
                Modifier.padding(8.dp).fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
        }

        if (state.isCurrencyDialogOpen) LazyColumn(
            Modifier.background(Color.Black.copy(alpha = .2f))
                .padding(horizontal = 8.dp).padding(top = 16.dp)
                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                .padding(horizontal = 8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            items(state.availableCurrencies) {
                Text(
                    "${it.code.value} - ${it.fullName}",
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.fillMaxWidth().clip(CircleShape).clickable {
                        onSelectElementFromDialog(it)
                    }.padding(8.dp).padding(start = 8.dp)
                )
            }
        }
    }
}