package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.model.RemarkRating
import seepick.localsportsclub.service.search.FullNumericComparator
import seepick.localsportsclub.service.search.RemarkRatingSearchOption
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.WidthOrFill

@Composable
fun <T> RemarkRatingSearchField(searchOption: RemarkRatingSearchOption<T>, tooltip: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.ClickableSearchText(tooltip = tooltip)
        if (searchOption.enabled) {
            DropDownTextField(
                items = FullNumericComparator.entries,
                selectedItem = searchOption.searchComparator,
                onItemSelected = { searchOption.updateSearchComparator(it) },
                enabled = searchOption.enabled,
                textSize = WidthOrFill.Width(80.dp),
                useSlimDisplay = true,
            )
            Spacer(Modifier.width(5.dp))
            DropDownTextField(
                items = RemarkRating.entries,
                itemFormatter = { it.emoji },
                selectedItem = searchOption.searchRating,
                onItemSelected = { searchOption.updateSearchRating(it) },
                enabled = searchOption.enabled,
                textSize = WidthOrFill.Width(90.dp),
                useSlimDisplay = true,
            )
        }
    }
}
