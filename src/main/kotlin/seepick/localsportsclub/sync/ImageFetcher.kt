package seepick.localsportsclub.sync

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import seepick.localsportsclub.api.requireStatusOk
import seepick.localsportsclub.service.FileSystemImageStorage
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.httpClient
import java.io.File

interface ImageFetcher {
    suspend fun saveVenueImage(venueId: Int, url: Url)
}

class HttpImageFetcher(
    private val imageStorage: ImageStorage,
    private val httpClient: HttpClient,
) : ImageFetcher {
    companion object {
        private val imageUrl =
            Url("https://storage.googleapis.com/download/storage/v1/b/usc-pro-uscweb-live-media/o/de-live%2FvenueCatalog_311x175_bmrtjlmxbveot0zlvwuj_1727358338834864.png?generation=1727358339345039&alt=media")

        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                HttpImageFetcher(FileSystemImageStorage(File("delme")), httpClient)
                    .saveVenueImage(1, imageUrl)
            }
        }
    }

    override suspend fun saveVenueImage(venueId: Int, url: Url) {
        val response = httpClient.get(url.toString())
        response.requireStatusOk()
        val bytes = response.body<ByteArray>()
        val extension = imageUrl.encodedPath.substringAfterLast(".")
        imageStorage.saveVenue(venueId, bytes, extension)
    }
}
