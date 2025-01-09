package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.io.File

fun FileResolver.resolveVenueImage(fileName: String) = File(resolve(DirectoryEntry.VenueImages), fileName)

interface ImageStorage {
    fun saveVenueImage(fileName: String, bytes: ByteArray)
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
