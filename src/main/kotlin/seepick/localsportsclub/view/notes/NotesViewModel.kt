package seepick.localsportsclub.view.notes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.service.SinglesService

class NotesViewModel(
    private val singlesService: SinglesService,
) : ViewModel(), ApplicationLifecycleListener {

    var notes by mutableStateOf("")
        private set
    var homeLatitude: Double? by mutableStateOf(null)
        private set
    var homeLongitude: Double? by mutableStateOf(null)
        private set

    override fun onStartUp() {
        notes = singlesService.readNotes()
        singlesService.readHome()?.also {
            homeLatitude = it.latitude
            homeLongitude = it.longitude
        }
    }

    fun onLatitudeEntered(string: String) {
        if (string.isEmpty()) {
            homeLatitude = null
        } else {
            string.toDoubleOrNull()?.also {
                homeLatitude = it
                maybeUpdateHome()
            }
        }
    }

    fun onLongitudeEntered(string: String) {
        if (string.isEmpty()) {
            homeLongitude = null
        } else {
            string.toDoubleOrNull()?.also {
                homeLongitude = it
                maybeUpdateHome()
            }
        }
    }

    private fun maybeUpdateHome() {
        if (homeLatitude != null && homeLongitude != null) {
            singlesService.updateHome(Location(longitude = homeLongitude!!, latitude = homeLatitude!!))
        }
    }

    fun notesUpdated(value: String) {
        notes = value
    }

    override fun onExit() {
        singlesService.updateNotes(notes)
    }
}
