package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.ModifierWith
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.venue.VenueImage

sealed interface SortableColumn {
    val isEnabled: Boolean get() = this is Enabled

    class Enabled(val initialDirection: SortDirection = SortDirection.Asc) : SortableColumn
    object Disabled : SortableColumn
}

data class TableColumn<T>(
    val header: VisualIndicator,
    val size: WidthOrWeight,
    val renderer: CellRenderer<T>,
    val initialSortDirection: SortDirection = SortDirection.Asc,
    val sorting: SortableColumn = SortableColumn.Enabled(initialSortDirection),
    val tooltip: String? = null,
    var sortValueExtractor: ((T) -> Any?)? = null,
) {
    init {
        if (sortValueExtractor == null) {
            when (renderer) {
                is CellRenderer.CustomRenderer -> if (sorting.isEnabled) error("No sort value extractor defined and not a TextRenderer!")
                is CellRenderer.TextRenderer -> sortValueExtractor = renderer.sortExtractor
            }
        }
    }
}

fun <T> tableColumnVenueImage(imageFileName: (T) -> String?): TableColumn<T> = TableColumn(
    header = VisualIndicator.StringIndicator("Image"),
    size = WidthOrWeight.Width(70.dp),
    renderer = CellRenderer.CustomRenderer { item, col ->
        Row(ModifierWith(col.size).height(30.dp)) {
            VenueImage(imageFileName(item))
        }
    },
    sorting = SortableColumn.Disabled,
)

fun <T> tableColumnFavorited(isFavorited: (T) -> Boolean): TableColumn<T> = iconImageColumn(
    header = Lsc.icons.favoritedIndicator,
    flagExtractor = isFavorited, Icons.Lsc.favorited2,
    tooltip = "Favorited",
    initialSortDirection = SortDirection.Desc,
)

fun <T> tableColumnWishlisted(isWishlisted: (T) -> Boolean): TableColumn<T> = iconImageColumn(
    header = Lsc.icons.wishlistedIndicator,
    flagExtractor = isWishlisted,
    icons = Icons.Lsc.wishlisted2,
    tooltip = "Wishlisted",
    initialSortDirection = SortDirection.Desc,
)

private fun <T> iconImageColumn(
    header: VisualIndicator,
    flagExtractor: (T) -> Boolean,
    icons: Pair<ImageBitmap, ImageBitmap>,
    tooltip: String?,
    initialSortDirection: SortDirection = SortDirection.Asc,
): TableColumn<T> = TableColumn(
    header = header,
    size = WidthOrWeight.Width(50.dp),
    renderer = CellRenderer.CustomRenderer { item, col ->
        Row(ModifierWith(col.size).height(30.dp), horizontalArrangement = Arrangement.Center) {
            Image(if (flagExtractor(item)) icons.first else icons.second, null)
        }
    },
    sortValueExtractor = { flagExtractor(it) },
    tooltip = tooltip,
    initialSortDirection = initialSortDirection,
)
