package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.service.search.IntSearchComparator
import seepick.localsportsclub.service.search.RatingSearchOption
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.WidthOrFill

@Composable
fun <T> RatingSearchField(searchOption: RatingSearchOption<T>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.ClickableSearchText()
        if (searchOption.enabled) {
            DropDownTextField(
                items = IntSearchComparator.entries,
                selectedItem = searchOption.searchComparator,
                onItemSelected = { searchOption.updateSearchComparator(it) },
                enabled = searchOption.enabled,
                textSize = WidthOrFill.Width(80.dp),
                useSlimDisplay = true,
            )
            Spacer(Modifier.width(5.dp))
            DropDownTextField(
                items = Rating.entries,
                selectedItem = searchOption.searchRating,
                onItemSelected = { searchOption.updateSearchRating(it) },
                enabled = searchOption.enabled,
                textSize = WidthOrFill.Width(140.dp),
                useSlimDisplay = true,
            )
        }
    }
}
