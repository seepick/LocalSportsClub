package seepick.localsportsclub.view.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.model.Rating

@Composable
fun RatingPanel(
    selectedRating: Rating,
    onRatingSelected: (Rating) -> Unit,
    enabled: Boolean = true,
) {
    DropDownTextField(
        label = "Rating",
        items = Rating.entries,
        selectedItem = selectedRating,
        onItemSelected = onRatingSelected,
        enabled = enabled,
        textWidth = 150.dp,
    )
}
