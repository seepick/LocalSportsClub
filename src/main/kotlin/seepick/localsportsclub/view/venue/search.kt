package seepick.localsportsclub.view.venue

import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.service.model.Venue

class VenueSearch {

    private val log = logger {}
    private val predicates = mutableListOf<(Venue) -> Boolean>()

    private val terms = mutableListOf<String>()
    //    var dateRange: Pair<DateTime, DateTime>? = null,

    fun clearTerm() {
        terms.clear()
        resetPredicates()
    }

    fun setTerm(newTerm: String) {
        require(newTerm.isNotEmpty())
        terms.clear()
        terms.addAll(newTerm.split(" "))
        log.debug { "Set search terms to: $terms" }
        resetPredicates()
    }

    private fun resetPredicates() {
        predicates.clear()
        if (terms.isNotEmpty()) {
            predicates += { venue -> terms.all { term -> venue.name.lowercase().contains(term) } }
        }
    }

    fun matches(venue: Venue): Boolean =
        predicates.all { it(venue) }
}

@Composable
fun SearchInput(
    viewModel: VenueViewModel = koinViewModel(),
) {
    var searchTerm by remember { mutableStateOf("") }
    // https://developer.android.com/develop/ui/compose/text/user-input
    OutlinedTextField(
        value = searchTerm,
        label = { Text("Search") },
        singleLine = true,
        modifier = Modifier
            .width(200.dp)
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Escape && e.type == KeyEventType.KeyUp && searchTerm != "") {
                    searchTerm = ""
                    viewModel.setSearchTerm(searchTerm)
                }
                false
            },
        onValueChange = {
            searchTerm = it
            viewModel.setSearchTerm(searchTerm)
        }
    )
}
