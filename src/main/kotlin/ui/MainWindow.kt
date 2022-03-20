package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import data.Result
import data.model.Rate
import data.nbpapi.responses.NbpRepository
import data.util.clearNumbers
import data.util.roundCurrency
import kotlinx.coroutines.launch
import org.kodein.di.compose.localDI
import org.kodein.di.instance

sealed interface MainState {
    object Loading : MainState
    data class Error(val errorMessage: String) : MainState
    data class Loaded(
        val inputString: String,
        val inputValue: Double,
        val selectedRate: Rate,
        val rates: List<Rate>
    ) : MainState
}

@Composable
fun MainRoute() {
    var uiState by remember { mutableStateOf<MainState>(MainState.Loading) }
    val scope = rememberCoroutineScope()
    val repository: NbpRepository by localDI().instance()


    fun loadData() = scope.launch {
        println("loadingdata")
        uiState = when (val rates = repository.getRates()) {
            is Result.Error -> MainState.Error(rates.exception.localizedMessage.toString())
            is Result.Success -> MainState.Loaded(
                inputString = "10.0",
                inputValue = 10.0,
                selectedRate = Rate(
                    code = "PLN",
                    value = 1.0,
                    fullName = ""
                ),
                rates = rates.data.rates
            )
        }
    }

    SideEffect {
        if (uiState is MainState.Loading) loadData()
    }

    when (val state = uiState) {
        is MainState.Error -> ErrorWindow({ loadData() }, state.errorMessage)
        is MainState.Loaded -> ContentWindow(
            onchange = {
                uiState = try {
                    state.copy(
                        inputString = it.clearNumbers(state.inputString),
                        inputValue = it.clearNumbers(state.inputString).toDouble()
                    )
                } catch (_: Exception) {
                    state
                }
            },
            onChangeCurrency = { uiState = state.copy(selectedRate = it) },
            state = state
        )
        MainState.Loading -> LoadingAnimation()
    }
}

@Composable
fun LoadingAnimation() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text("Ładowanie...")

    }
}

@Composable
fun ErrorWindow(onReload: () -> Unit, message: String) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(Modifier.background(Color.Red.copy(alpha = .4f))) {
            Button({
                onReload()
            }) {
                Text("Odświerz")
            }
            Text("Error: \n${message}")
        }
    }
}

@Composable
fun ContentWindow(
    onchange: (String) -> Unit,
    onChangeCurrency: (Rate) -> Unit,
    state: MainState.Loaded
) {
    Column {
        Row {
            val textStyle = MaterialTheme.typography.h3.copy(fontFamily = FontFamily.Monospace)
            BasicTextField(
                state.inputString,
                { onchange(it) },
                Modifier.weight(1f),
                textStyle = textStyle,
                maxLines = 1,
            )
            Text(state.selectedRate.code, style = textStyle)
        }
        LazyColumn(Modifier.weight(1f)) {
            items(state.rates) {
                RateElement(it, ((state.inputValue * state.selectedRate.value) / it.value).roundCurrency()) {
                    onChangeCurrency(it)
                }
            }
        }
    }
}

@Composable
fun RateElement(rate: Rate, calculatedAmount: Double, onClick: () -> Unit) {
    val style = MaterialTheme.typography.h6
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Text(
            if (rate.fullName.length > 22) {
                rate.fullName.slice(0..19) + "..."
            } else rate.fullName, style = style
        )
        Text("$calculatedAmount ${rate.code}", style = style)
    }
}