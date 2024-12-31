package seepick.localsportsclub.view.notes

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.service.SinglesService

class NotesViewModel(
    private val singlesService: SinglesService,
) : ViewModel(), ApplicationLifecycleListener {

    var notes by mutableStateOf("")
        private set

    override fun onStartUp() {
        notes = singlesService.readNotes()
    }

    fun notesUpdated(value: String) {
        notes = value
    }

    override fun onExit() {
        singlesService.updateNotes(notes)
    }
}

@Composable
fun NotesScreen(
    viewModel: NotesViewModel = koinViewModel()
) {
    val state = rememberScrollState()
    Box {
        TextField(
            value = viewModel.notes,
            onValueChange = {
                viewModel.notesUpdated(it)
            },
            modifier = Modifier.fillMaxSize(1.0f)
                .verticalScroll(state)
        )
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(
                scrollState = state
            ),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
        )
    }
}
