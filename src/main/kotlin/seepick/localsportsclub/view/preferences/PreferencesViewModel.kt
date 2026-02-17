package seepick.localsportsclub.view.preferences

import androidx.compose.material.SnackbarDuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.seepick.uscclient.ConnectionVerificationResult
import com.github.seepick.uscclient.UscApi
import com.github.seepick.uscclient.UscConnector
import com.github.seepick.uscclient.plan.Plan
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.ApplicationLifecycleListener
import seepick.localsportsclub.gcal.GcalConnectionTest
import seepick.localsportsclub.gcal.RealGcalService
import seepick.localsportsclub.service.FileEntry
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.singles.SinglesService
import seepick.localsportsclub.view.SnackbarService
import seepick.localsportsclub.view.SnackbarType
import seepick.localsportsclub.view.common.launchBackgroundTask
import seepick.localsportsclub.view.common.launchViewTask
import seepick.localsportsclub.view.shared.SharedModel

class PreferencesViewModel(
    private val singlesService: SinglesService,
    private val snackbarService: SnackbarService,
    private val sharedModel: SharedModel,
    private val uscConnector: UscConnector,
    private val api: UscApi,
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
        launchBackgroundTask(
            "Failed to verify the USC connection.",
            doBefore = { isUscConnectionVerifying = true },
            doFinally = { isUscConnectionVerifying = false }) {
            log.info { "Verifying USC connection ..." }
            val credentials = entity.buildCredentials()
            require(credentials != null) { "Credentials cannot be null here!" }
            when (val loginResult = uscConnector.verifyConnection(credentials)) {
                ConnectionVerificationResult.Success -> {
                    singlesService.verifiedUscCredentials = credentials
                    sharedModel.verifiedUscUsername.value = credentials.username
                    sharedModel.verifiedUscPassword.value = credentials.password
                    initialUscInfoOnLoginSuccess()
                    snackbarService.show("USC login was successful 🔐✅")
                }

                is ConnectionVerificationResult.Failure -> {
                    singlesService.verifiedUscCredentials = null
                    sharedModel.verifiedUscUsername.value = null
                    sharedModel.verifiedUscPassword.value = null
                    snackbarService.show(
                        message = "${loginResult.message} 🔐❌",
                        type = SnackbarType.Warn,
                        duration = SnackbarDuration.Long,
                    )
                }
            }
        }
    }

    private suspend fun initialUscInfoOnLoginSuccess() {
        if (entity.country == null && entity.city == null || singlesService.plan == null) {
            log.debug { "Fetching additional membership data for pre-fill-in." }
            val membership = api.fetchMembership()
            if (entity.country == null && entity.city == null) {
                entity.country = membership.country
                entity.city = membership.city
            }
            if (singlesService.plan == null) {
                // TODO what if plan changes?! then it needs to overwritten here, and not null-checked-out
                singlesService.plan = membership.plan
                plan = membership.plan
            }
        }
    }

    fun verifyGcalConnection() {
        launchBackgroundTask(
            "Error during testing the Google Calendar connection.",
            doBefore = { isGcalConnectionVerifying = true },
            doFinally = { isGcalConnectionVerifying = false }) {
            log.info { "Testing GCal connection ..." }
            val calendarId = entity.calendarId
            when (val result = gcalService.testConnection(calendarId)) {
                is GcalConnectionTest.Fail -> {
                    singlesService.verifiedGcalId = null
                    sharedModel.verifiedGcalId.value = null
                    snackbarService.show(
                        message = "Google connection failed 📆❌\n${result.message}",
                        type = SnackbarType.Warn,
                        duration = SnackbarDuration.Long,
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
