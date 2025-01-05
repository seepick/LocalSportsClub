package seepick.localsportsclub.sync

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import seepick.localsportsclub.serializerLenient
import seepick.localsportsclub.service.FileSystemImageStorage
import seepick.localsportsclub.service.ImageStorage
import seepick.localsportsclub.service.httpClient
import seepick.localsportsclub.service.safeGet
import java.io.File

interface Downloader {
    suspend fun downloadVenueImage(url: Url): ByteArray
}

object NoopDownloader : Downloader {
    private val log = logger {}

    override suspend fun downloadVenueImage(url: Url): ByteArray {
        log.info { "Noop image fetch." }
        return ImageStorage.defaultVenueImage
    }
}

class HttpDownloader(
    private val httpClient: HttpClient,
) : Downloader {
    private val log = logger {}

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                val imageUrl =
//                    Url("https://storage.googleapis.com/download/storage/v1/b/usc-pro-uscweb-live-media/o/de-live%2FvenueCatalog_311x175_bmrtjlmxbveot0zlvwuj_1727358338834864.png?generation=1727358339345039&alt=media")
                    Url("https://storage.googleapis.com/download/storage/v1/b/usc-pro-uscweb-live-media/o/de-live%2Foriginal_1680x945_e1cbnfnj6zdwdi2kght3_1727428109645212.png?generation=1727428109770038&amp;alt=media")
                val bytes = HttpDownloader(httpClient).downloadVenueImage(imageUrl)
                FileSystemImageStorage(File(".")).saveVenueImage("foo.png", bytes)
            }
        }
    }

    override suspend fun downloadVenueImage(url: Url): ByteArray {
        val realUrl = ensureGoogleStorageLink(url)
        val response = httpClient.safeGet(realUrl)
        return response.body<ByteArray>()
    }

    private suspend fun ensureGoogleStorageLink(url: Url): Url =
        if (url.toString().contains("/download/")) {
            log.debug { "Getting alternative Google storage download URL for: $url" }
            val cleanedUrl = url.toString().replace("/download/", "/")
            val response = httpClient.safeGet(Url(cleanedUrl))
            val json = serializerLenient.parseToJsonElement(response.bodyAsText())
            Url(json.jsonObject["mediaLink"]!!.jsonPrimitive.content)
        } else url

}

/*
{
  "kind": "storage#object",
  "id": "usc-pro-uscweb-live-media/de-live/original_1680x945_e1cbnfnj6zdwdi2kght3_1727428109645212.png/1727428109770038",
  "selfLink": "https://www.googleapis.com/storage/v1/b/usc-pro-uscweb-live-media/o/de-live%2Foriginal_1680x945_e1cbnfnj6zdwdi2kght3_1727428109645212.png",
  "mediaLink": "https://storage.googleapis.com/download/storage/v1/b/usc-pro-uscweb-live-media/o/de-live%2Foriginal_1680x945_e1cbnfnj6zdwdi2kght3_1727428109645212.png?generation=1727428109770038&alt=media",
  "name": "de-live/original_1680x945_e1cbnfnj6zdwdi2kght3_1727428109645212.png",
  "bucket": "usc-pro-uscweb-live-media",
  "generation": "1727428109770038",
  "metageneration": "1",
  "contentType": "image/png",
  "storageClass": "REGIONAL",
  "size": "1957232",
  "md5Hash": "5/6RkINuNKenCyHNgg2sxw==",
  "crc32c": "NqRCTg==",
  "etag": "CLa67pbj4ogDEAE=",
  "timeCreated": "2024-09-27T09:08:29.772Z",
  "updated": "2024-09-27T09:08:29.772Z",
  "timeStorageClassUpdated": "2024-09-27T09:08:29.772Z",
  "timeFinalized": "2024-09-27T09:08:29.772Z"
}
 */
