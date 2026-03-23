package seepick.localsportsclub.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.view.common.Tooltip

enum class Screen(
    val label: String,
    val icon: ImageVector,
    val tooltip: String,
) {
    Activities(
        label = "Activities",
        tooltip = "View available activities",
        icon = Lsc.icons.activities,
    ),
    Freetrainings(
        label = "Freetrainings",
        tooltip = "View available freetrainings",
        icon = Lsc.icons.freetrainings,
    ),
    Venues(
        label = "Venues",
        tooltip = "View all venues",
        icon = Lsc.icons.venues,
    ),
    Notes(
        label = "Notes",
        tooltip = "Write personal remarks",
        icon = Lsc.icons.notes,
    ),
    Preferences(
        label = "Preferences",
        tooltip = "Change application configuration",
        icon = Lsc.icons.preferences,
    );
}

@Composable
fun NavigationScreen(
    selectedScreen: Screen,
    selectScreen: (Screen) -> Unit,
) {
    TabRow(
        selectedTabIndex = Screen.entries.indexOf(selectedScreen),
        modifier = Modifier.width(700.dp)
    ) {
        Screen.entries.forEach { screen ->
            val isSelected = selectedScreen == screen
            Tooltip("${screen.tooltip} (CMD+${screen.ordinal + 1})") {
                Tab(
                    text = { Text(text = screen.label) },
                    selected = isSelected,
                    onClick = { selectScreen(screen) },
                    selectedContentColor = Lsc.colors.onPrimary,
                    modifier = Modifier
                        .background(if (isSelected) Lsc.colors.primaryDarker else Lsc.colors.primary),
                    icon = {
                        Icon(imageVector = screen.icon, contentDescription = null)
                    }
                )
            }
        }
    }
}
