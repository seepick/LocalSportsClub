package seepick.localsportsclub.view

import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult

class TestableSnackbarService : SnackbarService {
    val shownParams = mutableListOf<SnackbarParam>()
    override fun initializeSnackbarHostState(snackbarHostState: SnackbarHostState) {
    }

    override fun show(
        message: String,
        actionLabel: String?,
        duration: SnackbarDuration,
        onResult: (SnackbarResult) -> Unit
    ) {
        shownParams += SnackbarParam(message, actionLabel, duration, onResult)
    }
}

data class SnackbarParam(
    val message: String,
    val actionLabel: String?,
    val duration: SnackbarDuration,
    val onResult: (SnackbarResult) -> Unit,
)
