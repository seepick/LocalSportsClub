package seepick.localsportsclub.view.venue

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.resolveVenueImage


@Composable
fun VenueImage(imageFileName: String?) {
    val image = if (imageFileName == null) {
        ImageStorage.defaultVenueImageBitmap
    } else {
        val imageFile = FileResolver.resolveVenueImage(imageFileName)
        require(imageFile.exists()) { "Venue image file doesn't exist: ${imageFile.absolutePath}" }
        remember(imageFile) { loadImageBitmap(imageFile.inputStream()) }
    }
    Image(
        BitmapPainter(image), null, contentScale = ContentScale.Fit,
    )
}
