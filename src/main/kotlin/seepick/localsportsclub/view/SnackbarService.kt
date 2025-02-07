package seepick.localsportsclub.view

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface SnackbarService {
    val events: Flow<SnackbarEvent>

    suspend fun show(
        message: String,
        type: SnackbarType = SnackbarType.Info,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onResult: (SnackbarResult) -> Unit = {},
    ) {
        show(
            SnackbarEvent(
                content = SnackbarContent.TextContent(message),
                actionLabel = actionLabel,
                onResult = onResult,
                duration = duration,
                type = type,
            )
        )
    }

    suspend fun show(
        event: SnackbarEvent,
    )
}

sealed interface SnackbarContent {
    data class TextContent(val message: String) : SnackbarContent
    data class CustomContent(val composable: @Composable () -> Unit) : SnackbarContent
}

data class SnackbarEvent(
    val content: SnackbarContent,
    val type: SnackbarType = SnackbarType.Info,
    val actionLabel: String? = null,
    val onResult: ((SnackbarResult) -> Unit)? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
) {
    companion object {
        operator fun invoke(
            message: String,
            type: SnackbarType = SnackbarType.Info,
            actionLabel: String? = null,
            duration: SnackbarDuration = SnackbarDuration.Short,
        ) = SnackbarEvent(
            content = SnackbarContent.TextContent(message),
            type = type,
            actionLabel = actionLabel,
            duration = duration,
        )
    }
}

class SnackbarServiceViewModel : ViewModel(), SnackbarService {

    private val log = logger {}
    private val _events = Channel<SnackbarEvent>()
    override val events: Flow<SnackbarEvent> = _events.receiveAsFlow()

    override suspend fun show(event: SnackbarEvent) {
        log.debug { "showing snackbar $event" }
        _events.send(event)
    }
}

enum class SnackbarType {
    Info,
    Warn, // user can do something about it (e.g. change credentials)
    Error, // user can't do anything about it (e.g. internal server error)
    ;
}
