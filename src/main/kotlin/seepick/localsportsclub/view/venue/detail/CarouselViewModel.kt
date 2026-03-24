package seepick.localsportsclub.view.venue.detail

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.lifecycle.ViewModel
import com.github.seepick.uscclient.UscApi
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.jetbrains.skia.Image
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.launchBackgroundTask
import seepick.localsportsclub.view.shared.SharedModel

class CarouselViewModel(
    private val shared: SharedModel,
    private val api: UscApi,
    private val fileResolver: FileResolver,
    private val httpClient: HttpClient,
) : ViewModel() {
    private val log = KotlinLogging.logger {}

    var isInitialized by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var dialogTitle by mutableStateOf("")
        private set
    private val imageUrls = mutableListOf<String>()
    val totalImageCount by derivedStateOf { imageUrls.size }
    var currentImageIndex by mutableStateOf(0)
        private set
    var currentImageData: ImageBitmap? by mutableStateOf(null)
        private set
    val hasPreviousImage by derivedStateOf { currentImageIndex > 0 }
    val hasNextImage by derivedStateOf { currentImageIndex < totalImageCount - 1 }

    fun onVenueDetailImageClicked(venue: Venue) {
        log.debug { "onVenueDetailImageClicked($venue)" }
        isInitialized = false
        isLoading = false
        imageUrls.clear()
        currentImageData = null
        currentImageIndex = 0
        shared.carouselVenue.value = venue
    }

    fun loadPicUrls() {
        val venue = shared.carouselVenue.value!!
        dialogTitle = venue.name // hack
        launchBackgroundTask("Loading carousel images failed", fileResolver) {
            log.debug { "Loading carousel images for: ${venue.name}" }
            imageUrls.addAll(api.fetchVenueDetail(venue.slug).carouselUrls.map { it.toString() })
//            delay(500)
//            imageUrls.addAll(
//                listOf(
//                    "https://static.vecteezy.com/system/resources/thumbnails/024/799/947/small/palm-tree-set-illustration-collection-transparent-background-ai-generated-png.png",
//                    "https://static.vecteezy.com/system/resources/thumbnails/016/774/410/small/heart-rate-pulse-on-transparent-background-free-png.png",
//                    "https://static.vecteezy.com/system/resources/thumbnails/024/858/386/small/flat-lay-view-of-the-butterfly-on-transparent-background-created-with-generative-ai-png.png",
//                )
//            )
            isInitialized = true
            if (imageUrls.isNotEmpty()) {
                loadCurrentPic()
            }
        }
    }

    fun loadCurrentPic() {
        log.debug { "loadCurrentPic() #${currentImageIndex + 1}/$totalImageCount" }
        currentImageData = null
        loadInBg {
            val bytes = httpClient.get(imageUrls[currentImageIndex]) {
                contentType(ContentType.Image.Any)
            }.body<ByteArray>()
            currentImageData = Image.makeFromEncoded(bytes).toComposeImageBitmap()
        }
    }

    fun showPrevious() {
        currentImageIndex--
        loadCurrentPic()
    }

    fun showNext() {
        currentImageIndex++
        loadCurrentPic()
    }

    fun dismiss() {
        shared.carouselVenue.value = null
    }

    private fun loadInBg(bgTask: suspend () -> Unit) {
        launchBackgroundTask(
            "Carousel image loading failed!", fileResolver,
            doBefore = { isLoading = true },
            doFinally = { isLoading = false },
            doTask = bgTask,
        )
    }
}
