package seepick.localsportsclub.view.preferences

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.api.LoginResult
import seepick.localsportsclub.api.PhpSessionId
import seepick.localsportsclub.api.UscApi
import seepick.localsportsclub.gcal.GcalConnectionTest
import seepick.localsportsclub.gcal.RealGcalService
import seepick.localsportsclub.service.FileEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.model.Plan
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.view.SnackbarService
import seepick.localsportsclub.view.SnackbarType
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

    fun onLatitudeChanged(value: Double?) {
        entity.homeLatitude = value
    }

    fun onLongitudeChanged(value: Double?) {
        entity.homeLongitude = value
    }

    fun testUscConnection() {
        executeBackgroundTask("Error during testing the USC connection.",
            doBefore = { isUscConnectingTesting = true },
            doFinally = { isUscConnectingTesting = false }) {
            log.info { "Testing USC connection ..." }
            when (val result = uscApi.login(entity.buildCredentials()!!)) {
                is LoginResult.Failure -> snackbarService.show(" ${result.message} 🔐❌", SnackbarType.Warn)
                is LoginResult.Success -> {
                    initialUscInfoOnLoginSuccess(result.phpSessionId)
                    snackbarService.show("USC login was successful 🔐✅")
                }
            }
        }
    }

    private suspend fun initialUscInfoOnLoginSuccess(phpSessionId: PhpSessionId) {
        if (entity.country == null && entity.city == null || singlesService.plan == null) {
            log.debug { "Fetching additional membership data for pre-fill-in." }
            val membership = uscApi.fetchMembership(phpSessionId)
            if (entity.country == null && entity.city == null) {
                entity.country = membership.country
                entity.city = membership.city
            }
            if (singlesService.plan == null) {
                singlesService.plan = membership.plan
                plan = membership.plan
            }
        }
    }

    fun testGcalConnection() {
        executeBackgroundTask("Error during testing the Google Calendar connection.",
            doBefore = { isGcalConnectingTesting = true },
            doFinally = { isGcalConnectingTesting = false }) {
            log.info { "Testing GCal connection ..." }
            when (val result = gcalService.testConnection(entity.calendarId)) {
                is GcalConnectionTest.Fail -> snackbarService.show(
                    "Google connection failed 📆❌\n${result.message}",
                    SnackbarType.Warn
                )

                GcalConnectionTest.Success -> snackbarService.show("Google connection succeeded 📆✅")
            }
        }
    }

    fun resetTokenCache() {
        val file = FileResolver.resolve(FileEntry.GoogleCredentialsCache)
        if (file.exists()) {
            log.debug { "About to delete google cache at: ${file.absolutePath}" }
            file.delete()
            snackbarService.show("Google login token cache successfully deleted 🗑️✅")
        } else {
            snackbarService.show("Nothing to delete, token cache is already empty 🤷🏻‍♂️")
        }
    }
}
