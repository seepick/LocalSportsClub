package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import net.coobird.thumbnailator.Thumbnails
import seepick.localsportsclub.LscConfig
import java.io.ByteArrayOutputStream
import java.io.File

fun FileResolver.resolveVenueImage(fileName: String) = File(resolve(DirectoryEntry.VenueImages), fileName)

interface ImageStorage {
    fun saveAndResizeVenueImage(fileName: String, bytes: ByteArray)
}

class MemorizableImageStorage : ImageStorage {
    val savedVenueImages = mutableListOf<Pair<String, ByteArray>>()
    override fun saveAndResizeVenueImage(fileName: String, bytes: ByteArray) {
        savedVenueImages += fileName to bytes
    }
}

class FileSystemImageStorage(
    private val venueImagesFolder: File,
) : ImageStorage {
    private val log = logger {}

    override fun saveAndResizeVenueImage(fileName: String, bytes: ByteArray) {
        val resizedBytes = resizeImage(bytes, LscConfig.downloadImageSize)
        val target = File(venueImagesFolder, fileName)
        log.debug { "Saving image to: ${target.absolutePath}" }
        if (target.exists()) {
            target.delete()
        }
        target.writeBytes(resizedBytes)
    }
}

private fun resizeImage(original: ByteArray, size: Pair<Int, Int>, format: String = "png"): ByteArray {
    val output = ByteArrayOutputStream()
    Thumbnails.of(original.inputStream())
        .size(size.first, size.second) // first size, then ratio!
        .keepAspectRatio(true)
        .outputFormat(format)
        .outputQuality(1.0)
        .toOutputStream(output)
    return output.toByteArray()
}
