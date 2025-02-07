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
import seepick.localsportsclub.view.common.launchBackgroundTask
import seepick.localsportsclub.view.common.launchViewTask
import seepick.localsportsclub.view.shared.SharedModel

class PreferencesViewModel(
    private val singlesService: SinglesService,
    private val uscApi: UscApi,
    private val snackbarService: SnackbarService,
    private val sharedModel: SharedModel,
) : ViewModel(), ApplicationLifecycleListener {

    private val gcalService = RealGcalService()
    private val log = logger {}
    val entity = PreferencesViewEntity()
    var isUscConnectionVerifying: Boolean by mutableStateOf(false)
        private set
    var isGcalConnectionVerifying: Boolean by mutableStateOf(false)
        private set
    var plan: Plan? by mutableStateOf(null)

    val verifiedGcalId = sharedModel.verifiedGcalId
    val verifiedUscUsername = sharedModel.verifiedUscUsername
    val verifiedUscPassword = sharedModel.verifiedUscPassword

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

    fun verifyUscConnection() {
        launchBackgroundTask("Failed to verify the USC connection.",
            doBefore = { isUscConnectionVerifying = true },
            doFinally = { isUscConnectionVerifying = false }) {
            log.info { "Verifying USC connection ..." }
            val credentials = entity.buildCredentials()!!
            when (val result = uscApi.login(credentials)) {
                is LoginResult.Failure -> {
                    singlesService.verifiedUscCredentials = null
                    sharedModel.verifiedUscUsername.value = null
                    sharedModel.verifiedUscPassword.value = null
                    snackbarService.show(" ${result.message} 🔐❌", SnackbarType.Warn)
                }

                is LoginResult.Success -> {
                    singlesService.verifiedUscCredentials = credentials
                    sharedModel.verifiedUscUsername.value = credentials.username
                    sharedModel.verifiedUscPassword.value = credentials.password
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

    fun verifyGcalConnection() {
        launchBackgroundTask("Error during testing the Google Calendar connection.",
            doBefore = { isGcalConnectionVerifying = true },
            doFinally = { isGcalConnectionVerifying = false }) {
            log.info { "Testing GCal connection ..." }
            val calendarId = entity.calendarId
            when (val result = gcalService.testConnection(calendarId)) {
                is GcalConnectionTest.Fail -> {
                    singlesService.verifiedGcalId = null
                    sharedModel.verifiedGcalId.value = null
                    snackbarService.show(
                        "Google connection failed 📆❌\n${result.message}", SnackbarType.Warn
                    )
                }

                GcalConnectionTest.Success -> {
                    singlesService.verifiedGcalId = calendarId
                    sharedModel.verifiedGcalId.value = calendarId
                    snackbarService.show("Google connection succeeded 📆✅")
                }
            }
        }
    }

    fun resetTokenCache() {
        val file = FileResolver.resolve(FileEntry.GoogleCredentialsCache)
        val snackbarMessage = if (file.exists()) {
            log.debug { "About to delete google cache at: ${file.absolutePath}" }
            file.delete()
            singlesService.verifiedGcalId = null
            "Google login token cache successfully deleted 🗑️✅"
        } else {
            "Nothing to delete, token cache is already empty 🤷🏻‍♂️"
        }
        launchViewTask("Failed to show snackbar") {
            snackbarService.show(snackbarMessage)
        }
    }

    fun setUscUsername(username: String) {
        entity.uscUsername = username
        sharedModel.verifiedUscUsername.value = null
        sharedModel.verifiedUscPassword.value = null
        if (singlesService.verifiedUscCredentials != null) {
            singlesService.verifiedUscCredentials = null
        }
    }

    fun setUscPassword(password: String) {
        entity.uscPassword = password
        sharedModel.verifiedUscUsername.value = null
        sharedModel.verifiedUscPassword.value = null
        if (singlesService.verifiedUscCredentials != null) {
            singlesService.verifiedUscCredentials = null
        }
    }

    fun setCalendarId(id: String) {
        entity.calendarId = id
        sharedModel.verifiedGcalId.value = null
        if (singlesService.verifiedGcalId != null) {
            singlesService.verifiedGcalId = null
        }
    }
}
