import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import di.mainModule
import org.kodein.di.compose.withDI
import ui.AppNavigation

@Composable
fun App() = withDI(mainModule) {
    AppNavigation()
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "\uD83D\uDCB0 Flying Currency Converter \uD83D\uDCA8",
        state = rememberWindowState(size = DpSize(450.dp, 600.dp)),
        resizable = false
    ) {
        App()
    }
}
