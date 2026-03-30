package seepick.localsportsclub.view.venue

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.service.resolveVenueImage
import seepick.localsportsclub.view.common.brighter
import seepick.localsportsclub.view.common.readImageBitmapFromClasspath
import seepick.localsportsclub.view.common.readImageBitmapFromFile
import seepick.localsportsclub.view.venue.detail.CarouselViewModel

private val defaultVenueImageBitmap: ImageBitmap = readImageBitmapFromClasspath("/defaultVenueImage.png")

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VenueImage(
    venue: Venue,
    drawBorder: Boolean = false,
    carouselModel: CarouselViewModel = koinInject(),
) {
    var isHovered by remember { mutableStateOf(false) }
    val image = if (venue.imageFileName == null) {
        defaultVenueImageBitmap
    } else {
        val fileResolver = koinInject<FileResolver>()
        val imageFile = fileResolver.resolveVenueImage(venue.imageFileName)
        require(imageFile.exists()) { "Venue image file doesn't exist: ${imageFile.absolutePath}" }
        remember(imageFile) { readImageBitmapFromFile(imageFile) }
    }
    Image(
        painter = BitmapPainter(image),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .clickable { carouselModel.onVenueDetailImageClicked(venue) }
            .pointerHoverIcon(PointerIcon.Hand)
            .onPointerEvent(PointerEventType.Enter) { isHovered = true }
            .onPointerEvent(PointerEventType.Exit) { isHovered = false }.let {
                if (drawBorder) it.border(
                    width = 1.dp,
                    color = if (isHovered) Lsc.colors.clickableNeutral.brighter() else Lsc.colors.clickableNeutral,
                ) else it
            }
    )
}
