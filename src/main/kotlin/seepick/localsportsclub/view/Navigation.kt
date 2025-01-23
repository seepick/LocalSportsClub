package seepick.localsportsclub.view

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

enum class Screen(
    val label: String,
    val icon: ImageVector,
) {
    Activities(label = "Activities", icon = Lsc.icons.activities),
    Freetrainings(label = "Freetrainings", icon = Lsc.icons.freetrainings),
    Venues(label = "Venues", icon = Lsc.icons.venues),
    Notes(label = "Notes", icon = Lsc.icons.notes),
    Preferefences(label = "Preferefences", icon = Lsc.icons.preferences);
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
            Tab(text = { Text(screen.label) },
                selected = selectedScreen == screen,
                onClick = { selectScreen(screen) },
                icon = {
                    Icon(imageVector = screen.icon, contentDescription = null)
                }
            )
        }
    }

//    Row {
//        Screen.entries.forEach { screen ->
//            Button(
//                enabled = selectedScreen != screen,
//                onClick = { selectScreen(screen) },
//            ) {
//                Text(text = screen.label)
//            }
//        }
//    }
}
