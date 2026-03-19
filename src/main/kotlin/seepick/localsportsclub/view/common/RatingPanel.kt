package seepick.localsportsclub.view.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.model.Rating

@Composable
fun RatingPanel(
    selectedRating: Rating,
    onRatingSelected: (Rating) -> Unit,
    enabled: Boolean = true,
) {
    DropDownTextField(
        items = Rating.entries,
        selectedItem = selectedRating,
        onItemSelected = onRatingSelected,
        enabled = enabled,
        textSize = WidthOrFill.Width(160.dp),
        useSlimDisplay = true,
        textAlign = TextAlign.Left,
        paddingMode = PaddingMode.Horizontal,
    )
}
