package seepick.localsportsclub.view.usage

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyShortPrint

@Composable
fun UsageView(
    viewModel: UsageViewModel = koinViewModel(),
    clock: Clock = koinInject(),
) {
    val year = clock.today().year
    Row {
        Text("Usage: ${viewModel.periodFirstDay.prettyShortPrint(year)}-${viewModel.periodLastDay.prettyShortPrint(year)}")
    }
}
