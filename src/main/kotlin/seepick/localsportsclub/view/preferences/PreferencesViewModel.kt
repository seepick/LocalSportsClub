package seepick.localsportsclub.view.preferences

import androidx.lifecycle.ViewModel
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.service.SinglesService

class PreferencesViewModel(
    private val singlesService: SinglesService,
) : ViewModel(), ApplicationLifecycleListener {

    val entity = PreferencesViewEntity()

    override fun onStartUp() {
        entity.setup(singlesService.readPreferences())
    }

    override fun onExit() {
        singlesService.updatePreferences(entity.buildPreferences())
    }

    fun onLatitudeEntered(string: String) {
        if (string.isEmpty()) {
            entity.homeLatitude = null
        } else {
            string.toDoubleOrNull()?.also {
                entity.homeLatitude = it
            }
        }
    }

    fun onLongitudeEntered(string: String) {
        if (string.isEmpty()) {
            entity.homeLongitude = null
        } else {
            string.toDoubleOrNull()?.also {
                entity.homeLongitude = it
            }
        }
    }
}
