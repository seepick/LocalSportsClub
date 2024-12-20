package seepick.localsportsclub.view.venue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VenuePanel(
    viewModel: VenueViewModel = koinViewModel(),
) {
    Column {
        Row {
            SearchInput()
        }
        VenuesTable()
    }
}
