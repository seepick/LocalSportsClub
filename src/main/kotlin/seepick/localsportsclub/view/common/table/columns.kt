package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.venue.VenueImage

fun <T> tableColumnVenueImage(imageFileName: (T) -> String?): TableColumn<T> {
    return TableColumn("Image", ColSize.Width(100.dp), CellRenderer.CustomRenderer { item, col ->
        Row(
            ModifierWith(col.size)
//            Modifier.width(110.dp)
                .height(30.dp).background(Color.Blue)
        ) {
            VenueImage(imageFileName(item))
        }
    }, sortingEnabled = false)
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
    TableColumn(header, ColSize.Width(60.dp), CellRenderer.CustomRenderer { item, col ->
        Row(ModifierWith(col.size).height(30.dp)) {
            Image(if (flagExtractor(item)) icons.first else icons.second, null)
        }
    }, sortValueExtractor = { flagExtractor(it) })
