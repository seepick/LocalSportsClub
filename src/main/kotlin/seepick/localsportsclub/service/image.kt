package seepick.localsportsclub.service

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.openFromClasspath
import java.io.File

fun FileResolver.resolveVenueImage(fileName: String) = File(resolve(DirectoryEntry.VenueImages), fileName)

interface ImageStorage {

    companion object {
        val defaultVenueImage: ByteArray = openFromClasspath("/defaultVenueImage.png").readAllBytes()
        val defaultVenueImageBitmap: ImageBitmap = loadImageBitmap(defaultVenueImage.inputStream())
    }

    fun saveVenueImage(fileName: String, bytes: ByteArray)
}

object NoopImageStorage : ImageStorage {
    private val log = logger {}
    override fun saveVenueImage(fileName: String, bytes: ByteArray) {
        log.debug { "Noop not saving venue image: $fileName" }
    }
}

class MemorizableImageStorage : ImageStorage {
    val savedVenueImages = mutableListOf<Pair<String, ByteArray>>()
    override fun saveVenueImage(fileName: String, bytes: ByteArray) {
        savedVenueImages += fileName to bytes
    }
}

class FileSystemImageStorage(
    private val venueImagesFolder: File,
) : ImageStorage {
    private val log = logger {}

    override fun saveVenueImage(fileName: String, bytes: ByteArray) {
        val target = File(venueImagesFolder, fileName)
        log.debug { "Saving image to: ${target.absolutePath}" }
        if (target.exists()) {
            target.delete()
        }
        target.writeBytes(bytes)
    }
}
