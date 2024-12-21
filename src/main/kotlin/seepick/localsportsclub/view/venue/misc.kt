package seepick.localsportsclub.view.venue

import androidx.compose.foundation.Image
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import seepick.localsportsclub.service.DirectoryEntry
import seepick.localsportsclub.service.FileResolver
import java.io.File

@Composable
fun VenueImage(venueId: Int?) {
    if (venueId == null) {
        Text("No Image")
    } else {
        val imageFile = File(FileResolver.resolve(DirectoryEntry.VenueImages), "$venueId.png")
        val image = remember(imageFile) { loadImageBitmap(imageFile.inputStream()) }
        Image(BitmapPainter(image), null, contentScale = ContentScale.Fit)
    }
}
