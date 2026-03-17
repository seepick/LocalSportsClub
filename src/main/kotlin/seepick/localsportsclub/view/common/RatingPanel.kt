package seepick.localsportsclub.view.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.model.Rating

val RatingPanelWidth = 160.dp

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
        textSize = WidthOrFill.Width(RatingPanelWidth),
        useSlimDisplay = true,
        textAlign = TextAlign.Left,
        paddingMode = PaddingMode.Horizontal,
    )
}
