package seepick.localsportsclub.view

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.view.common.executeViewTask

interface SnackbarService {
    fun initializeSnackbarHostState(snackbarHostState: SnackbarHostState)
    fun show(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onResult: (SnackbarResult) -> Unit = {},
    )
}

class SnackbarServiceViewModel : ViewModel(), SnackbarService {

    private val log = logger {}
    private lateinit var snackbarHostState: SnackbarHostState

    override fun initializeSnackbarHostState(snackbarHostState: SnackbarHostState) {
        log.debug { "initializeSnackbarHostState" }
        this.snackbarHostState = snackbarHostState
    }

    override fun show(
        message: String,
        actionLabel: String?,
        duration: SnackbarDuration,
        onResult: (SnackbarResult) -> Unit,
    ) {
        executeViewTask("Showing snackbar failed") {
            log.debug { "showing snackbar [$message]" }
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = duration,
            )
            onResult(result)
        }
    }
}
