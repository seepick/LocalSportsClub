package seepick.localsportsclub.view

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

enum class Screen(val label: String) {
    Activities(label = "Activities"),
    Freetrainings(label = "Freetrainings"),
    Venues(label = "Venues"),
    Notes(label = "Notes"),
}

@Composable
fun NavigationScreen(
    selectedScreen: Screen,
    selectScreen: (Screen) -> Unit,
) {
    Row {
        Screen.entries.forEach { screen ->
            Button(
                enabled = selectedScreen != screen,
                onClick = { selectScreen(screen) },
            ) {
                Text(text = screen.label)
            }
        }
    }
}
