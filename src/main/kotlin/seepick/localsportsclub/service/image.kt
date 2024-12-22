package seepick.localsportsclub.service

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.io.File

interface ImageStorage {

    companion object {
        val defaultVenueImage: ByteArray =
            ImageStorage::class.java.getResourceAsStream("/defaultVenueImage.png")!!.readAllBytes()
        val defaultVenueImageBitmap: ImageBitmap = loadImageBitmap(defaultVenueImage.inputStream())
    }

    fun saveVenue(fileName: String, bytes: ByteArray)
}

object NoopImageStorage : ImageStorage {
    private val log = logger {}
    override fun saveVenue(fileName: String, bytes: ByteArray) {
        log.debug { "Noop not saving venue image: $fileName" }
    }
}

class MemorizableImageStorage : ImageStorage {
    val savedVenues = mutableListOf<Pair<String, ByteArray>>()
    override fun saveVenue(fileName: String, bytes: ByteArray) {
        savedVenues += fileName to bytes
    }

}

class FileSystemImageStorage(
    private val venueImagesFolder: File,
) : ImageStorage {
    private val log = logger {}

    override fun saveVenue(fileName: String, bytes: ByteArray) {
        val target = File(venueImagesFolder, fileName)
        log.debug { "Saving image to: ${target.absolutePath}" }
        require(!target.exists())
        target.writeBytes(bytes)
    }
}
