package seepick.localsportsclub.view.preferences

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.api.LoginResult
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.gcal.GcalConnectionTest
import seepick.localsportsclub.gcal.RealGcalService
import seepick.localsportsclub.service.model.Plan
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.view.SnackbarService
import seepick.localsportsclub.view.common.executeBackgroundTask

class PreferencesViewModel(
    private val singlesService: SinglesService,
    private val uscApi: UscApi,
    private val snackbarService: SnackbarService,
) : ViewModel(), ApplicationLifecycleListener {

    private val gcalService = RealGcalService()
    private val log = logger {}
    val entity = PreferencesViewEntity()
    var isUscConnectingTesting: Boolean by mutableStateOf(false)
        private set
    var isGcalConnectingTesting: Boolean by mutableStateOf(false)
        private set
    var plan: Plan? by mutableStateOf(null)

    override fun onStartUp() {
        entity.setup(singlesService.preferences)
        plan = singlesService.plan
    }

    override fun onExit() {
        singlesService.preferences = entity.buildPreferences()
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

    fun testUscConnection() {
        executeBackgroundTask("Error during testing the USC connection.",
            doBefore = { isUscConnectingTesting = true },
            doFinally = { isUscConnectingTesting = false }) {
            log.info { "Testing USC connection ..." }
            when (val result = uscApi.login(entity.buildCredentials()!!)) {
                is LoginResult.Failure -> snackbarService.show("❌ ${result.message}")
                is LoginResult.Success -> {
                    if (entity.country == null && entity.city == null || singlesService.plan == null) {
                        log.debug { "Fetching additional membership data for pre-fillin." }
                        val membership = uscApi.fetchMembership(result.phpSessionId)
                        if (entity.country == null && entity.city == null) {
                            entity.country = membership.country
                            entity.city = membership.city
                        }
                        if (singlesService.plan == null) {
                            singlesService.plan = membership.plan
                            plan = membership.plan
                        }
                    }
                    snackbarService.show("✅ USC credentials are valid")
                }
            }
        }
    }

    fun testGcalConnection() {
        executeBackgroundTask("Error during testing the Google Calendar connection.",
            doBefore = { isGcalConnectingTesting = true },
            doFinally = { isGcalConnectingTesting = false }) {
            log.info { "Testing GCal connection ..." }
            when (gcalService.testConnection(entity.calendarId)) {
                GcalConnectionTest.Fail -> snackbarService.show("❌ GCal connection failed")
                GcalConnectionTest.Success -> snackbarService.show("✅ GCal connection succeeded")
            }
        }
    }
}
