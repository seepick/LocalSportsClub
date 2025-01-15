package seepick.localsportsclub.view.notes

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesScreen(
    viewModel: NotesViewModel = koinViewModel()
) {
    val state = rememberScrollState()
    Column {
        Box(modifier = Modifier.weight(1.0f)) {
            TextField(
                value = viewModel.notes, onValueChange = {
                    viewModel.notesUpdated(it)
                }, modifier = Modifier.verticalScroll(state)
            )
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(
                    scrollState = state
                ),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        }
        Row {
            Text("Home Location:", modifier = Modifier.align(Alignment.CenterVertically))
            Spacer(Modifier.width(5.dp))
            TextField(
                value = viewModel.homeLatitude?.toString() ?: "",
                maxLines = 1,
                label = { Text("Latitude") },
                modifier = Modifier.width(250.dp),
                onValueChange = {
                    viewModel.onLatitudeEntered(it)
                })
            Spacer(Modifier.width(5.dp))
            TextField(
                value = viewModel.homeLongitude?.toString() ?: "",
                maxLines = 1,
                label = { Text("Longitude") },
                modifier = Modifier.width(250.dp),
                onValueChange = {
                    viewModel.onLongitudeEntered(it)
                })
        }
    }
}
