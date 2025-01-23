package seepick.localsportsclub.view.notes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.view.common.LscVScroll

@Composable
fun NotesScreen(
    viewModel: NotesViewModel = koinViewModel()
) {
    val state = rememberScrollState()
    Column {
        Box {
            TextField(
                value = viewModel.notes,
                onValueChange = {
                    viewModel.notesUpdated(it)
                },
                modifier = Modifier
                    .fillMaxHeight(1f)
                    .fillMaxWidth(1f)
                    .verticalScroll(state)
            )
            LscVScroll(rememberScrollbarAdapter(state))
        }
    }
}
