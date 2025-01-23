package seepick.localsportsclub.view.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.service.singles.SinglesService

class NotesViewModel(
    private val singlesService: SinglesService,
) : ViewModel(), ApplicationLifecycleListener {

    var notes by mutableStateOf("")
        private set

    override fun onStartUp() {
        notes = singlesService.notes ?: ""
    }

    fun notesUpdated(value: String) {
        notes = value
    }

    override fun onExit() {
        singlesService.notes = notes
    }
}
