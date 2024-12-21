package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import java.io.File

interface ImageStorage {
    fun saveVenue(id: Int, bytes: ByteArray, extension: String)
}

object NoopImageStorage : ImageStorage {
    override fun saveVenue(id: Int, bytes: ByteArray, extension: String) {
    }
}

class FileSystemImageStorage(
    private val venueImagesFolder: File,
) : ImageStorage {
    private val log = logger {}

    override fun saveVenue(id: Int, bytes: ByteArray, extension: String) {
        val target = File(venueImagesFolder, "$id.$extension")
        log.debug { "Saving image to: ${target.absolutePath}" }
        require(!target.exists())
        target.writeBytes(bytes)
    }
}
