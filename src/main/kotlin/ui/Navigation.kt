package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun <T> NavigationHostElement(
    controller: NavigationController<T>,
    body: @Composable (current: T) -> Unit
) {
    body(controller.currentBackStackEntry.value)
}

class NavigationController<T>(start: T) {
    val backStack = mutableListOf(start)
    fun pop() {
        // size == 1 should never happen
        if (backStack.size > 1) backStack.removeLast()
        currentBackStackEntry.value = backStack.last()
    }

    var currentBackStackEntry: MutableState<T> = mutableStateOf(start)

    fun navigate(destination: T) {
        backStack.add(destination)
        currentBackStackEntry.value = destination
    }

    fun clearPathNavigate(destination: T) {
        backStack.clear()
        navigate(destination)
    }

    data class BackStackEntry<T>(
        val destination: T,
        val composable: @Composable () -> Unit
    )
}

@Composable
fun <T> rememberNavigationController(default: T) = remember { NavigationController(default) }

@Composable
fun AppNavigation() {
    var selectedItem by remember { mutableStateOf(0) }
    val navController = rememberNavigationController<MainNavigationDestinations>(MainNavigationDestinations.Exchange)
    Row(Modifier.fillMaxWidth()) {
        NavigationRail {
            sidebar.forEachIndexed { index, item ->
                NavigationRailItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.title, maxLines = 1) },
                    selected = selectedItem == index,
                    onClick = {
                        selectedItem = index
                        navController.navigate(item.destination)
                    }
                )
            }
        }
        Box(Modifier.fillMaxWidth()) {
            when (val current = navController.currentBackStackEntry.value) {
                MainNavigationDestinations.Exchange -> MainRoute()
                MainNavigationDestinations.GoldPrice -> GoldPriceRoute()
                is MainNavigationDestinations.Historical -> HistoricalRoute()
            }
        }
    }
}

val sidebar = listOf(
    SidebarDestination("Exchange", Icons.Default.EuroSymbol, MainNavigationDestinations.Exchange),
    SidebarDestination("Historical", Icons.Default.History, MainNavigationDestinations.Historical),
    SidebarDestination("Gold price", Icons.Default.LocalOffer, MainNavigationDestinations.GoldPrice),
)

data class SidebarDestination(
    val title: String,
    val icon: ImageVector,
    val destination: MainNavigationDestinations
)

sealed interface MainNavigationDestinations {
    object Exchange : MainNavigationDestinations
    object Historical : MainNavigationDestinations
    object GoldPrice : MainNavigationDestinations
}
