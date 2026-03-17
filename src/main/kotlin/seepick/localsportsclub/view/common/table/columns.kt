package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.service.model.Venue
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

fun <T> tableColumnVenueImage(venueMapper: (T) -> Venue): TableColumn<T> = TableColumn(
    header = VisualIndicator.StringIndicator(""),
    size = WidthOrWeight.Width(53.dp),
    renderer = CellRenderer.CustomRenderer { item, col ->
        Row(ModifierWith(col.size).height(30.dp)) {
            VenueImage(venueMapper(item))
        }
    },
    sorting = SortableColumn.Disabled,
)
