package seepick.localsportsclub.view

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.view.common.launchViewTask

interface SnackbarService {
    fun initCallback(callback: suspend (SnackbarData2) -> SnackbarResult)

    fun show(
        message: String,
        type: SnackbarType = SnackbarType.Info,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onResult: (SnackbarResult) -> Unit = {},
    ) {
        show(
            SnackbarData2(
                message = message,
                actionLabel = actionLabel,
                duration = duration,
                type = type,
            ), onResult
        )
    }

    fun show(
        data: SnackbarData2,
        onResult: (SnackbarResult) -> Unit = {},
    )
}

data class SnackbarData2(
    val message: String,
    val type: SnackbarType = SnackbarType.Info,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
)

class SnackbarServiceViewModel : ViewModel(), SnackbarService {

    private val log = logger {}
    private lateinit var callback: suspend (SnackbarData2) -> SnackbarResult

    override fun initCallback(callback: suspend (SnackbarData2) -> SnackbarResult) {
        log.debug { "initCallback..." }
        this.callback = callback
    }

    override fun show(
        data: SnackbarData2,
        onResult: (SnackbarResult) -> Unit,
    ) {
        launchViewTask("Showing snackbar failed") {
            log.debug { "showing snackbar $data" }
            val result = callback(data)
            onResult(result)
        }
    }
}

enum class SnackbarType {
    Info,
    Warn, // user can do something about it (e.g. change credentials)
    Error, // user can't do anything about it (e.g. internal server error)
    ;
}
