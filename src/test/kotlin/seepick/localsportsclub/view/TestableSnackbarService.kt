package seepick.localsportsclub.view

import androidx.compose.material.SnackbarResult

class TestableSnackbarService : SnackbarService {

    val shownParams = mutableListOf<SnackbarParam>()

    override fun initCallback(callback: suspend (SnackbarData2) -> SnackbarResult) {
    }

    override fun show(
        data: SnackbarData2,
        onResult: (SnackbarResult) -> Unit
    ) {
        shownParams += SnackbarParam(data, onResult)
    }
}

data class SnackbarParam(
    val data: SnackbarData2,
    val onResult: (SnackbarResult) -> Unit,
)
