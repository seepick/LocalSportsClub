package seepick.localsportsclub.view.venue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VenuePanel() {
    Column {
        Row {
            SearchInput()
        }
        Row(Modifier.weight(1.0f, true)) {
            VenuesTable()
            Spacer(Modifier.width(10.dp))
            VenuesDetail()
        }
    }
}

