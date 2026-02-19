package seepick.localsportsclub.view.venue

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import org.koin.compose.koinInject
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.resolveVenueImage
import seepick.localsportsclub.view.common.readImageBitmapFromClasspath
import seepick.localsportsclub.view.common.readImageBitmapFromFile

private val defaultVenueImageBitmap: ImageBitmap = readImageBitmapFromClasspath("/defaultVenueImage.png")

@Composable
fun VenueImage(imageFileName: String?) {
    val image = if (imageFileName == null) {
        defaultVenueImageBitmap
    } else {
        val fileResolver = koinInject<FileResolver>()
        val imageFile = fileResolver.resolveVenueImage(imageFileName)
        require(imageFile.exists()) { "Venue image file doesn't exist: ${imageFile.absolutePath}" }
        remember(imageFile) { readImageBitmapFromFile(imageFile) }
    }
    Image(
        painter = BitmapPainter(image),
        contentDescription = null,
        contentScale = ContentScale.Fit,
    )
}
