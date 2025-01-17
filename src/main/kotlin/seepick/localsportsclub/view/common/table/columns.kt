package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.ModifierWith
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.venue.VenueImage

fun <T> tableColumnVenueImage(imageFileName: (T) -> String?): TableColumn<T> {
    return TableColumn(
        "Image", WidthOrWeight.Width(70.dp),
        CellRenderer.CustomRenderer { item, col ->
            Row(ModifierWith(col.size).height(30.dp)) {
                VenueImage(imageFileName(item))
            }
        }, sortingEnabled = false
    )
}

fun <T> tableColumnFavorited(isFavorited: (T) -> Boolean): TableColumn<T> =
    iconImageColumn("Fav", isFavorited, Icons.Lsc.Favorites)

fun <T> tableColumnWishlisted(isWishlisted: (T) -> Boolean): TableColumn<T> =
    iconImageColumn("Wsh", isWishlisted, Icons.Lsc.Wishlists)

private fun <T> iconImageColumn(
    header: String,
    flagExtractor: (T) -> Boolean,
    icons: Pair<ImageBitmap, ImageBitmap>
): TableColumn<T> =
    TableColumn(header, WidthOrWeight.Width(50.dp), CellRenderer.CustomRenderer { item, col ->
        Row(ModifierWith(col.size).height(30.dp), horizontalArrangement = Arrangement.Center) {
            Image(if (flagExtractor(item)) icons.first else icons.second, null)
        }
    }, sortValueExtractor = { flagExtractor(it) })
