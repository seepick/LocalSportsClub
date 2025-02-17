package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.ModifierWith
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.venue.VenueImage

data class TableColumn<T>(
    val header: VisualIndicator,
    val size: WidthOrWeight,
    val renderer: CellRenderer<T>,
    val sortingEnabled: Boolean = true,
    var sortValueExtractor: ((T) -> Any?)? = null,
) {
    init {
        if (sortValueExtractor == null) {
            when (renderer) {
                is CellRenderer.CustomRenderer -> if (sortingEnabled) error("No sort value extractor defined and not a TextRenderer!")
                is CellRenderer.TextRenderer -> sortValueExtractor = renderer.sortExtractor
            }
        }
    }
}

fun <T> tableColumnVenueImage(imageFileName: (T) -> String?): TableColumn<T> =
    TableColumn(
        VisualIndicator.StringIndicator("Image"), WidthOrWeight.Width(70.dp),
        CellRenderer.CustomRenderer { item, col ->
            Row(ModifierWith(col.size).height(30.dp)) {
                VenueImage(imageFileName(item))
            }
        }, sortingEnabled = false
    )

fun <T> tableColumnFavorited(isFavorited: (T) -> Boolean): TableColumn<T> =
    iconImageColumn(Lsc.icons.favoritedIndicator, isFavorited, Icons.Lsc.favorited2)

fun <T> tableColumnWishlisted(isWishlisted: (T) -> Boolean): TableColumn<T> =
    iconImageColumn(Lsc.icons.wishlistedIndicator, isWishlisted, Icons.Lsc.wishlisted2)

private fun <T> iconImageColumn(
    header: VisualIndicator,
    flagExtractor: (T) -> Boolean,
    icons: Pair<ImageBitmap, ImageBitmap>
): TableColumn<T> =
    TableColumn(header, WidthOrWeight.Width(50.dp), CellRenderer.CustomRenderer { item, col ->
        Row(ModifierWith(col.size).height(30.dp), horizontalArrangement = Arrangement.Center) {
            Image(if (flagExtractor(item)) icons.first else icons.second, null)
        }
    }, sortValueExtractor = { flagExtractor(it) })
