package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import com.github.seepick.uscclient.UscApi
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.jetbrains.skia.Image
import org.koin.compose.koinInject
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.common.launchBackgroundTask
import seepick.localsportsclub.view.shared.SharedModel

class CarouselViewModel(
    private val shared: SharedModel,
    private val api: UscApi,
    private val fileResolver: FileResolver,
    private val httpClient: HttpClient,
) : ViewModel() {
    private val log = logger {}

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
        isInitialized = true
        imageUrls.clear()
        currentImageIndex = 0
        shared.carouselVenue.value = venue
    }

    fun loadPicUrls() {
        val venue = shared.carouselVenue.value!!
        dialogTitle = venue.name // hack
        launchBackgroundTask("Loading carousel images failed", fileResolver) {
            log.debug { "Loading carousel images" }
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
            loadCurrentPic()
        }
    }

    fun loadCurrentPic() {
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

@Composable
fun CarouselDialog(
    model: CarouselViewModel = koinInject(),
) {
    Dialog(onDismissRequest = model::dismiss) {
        Card(
            modifier = Modifier.size(width = 800.dp, height = 400.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            CarouselView()
        }
    }
}

@Composable
fun CarouselView(
    model: CarouselViewModel = koinInject(),
) {
    LaunchedEffect(key1 = true) {
        model.loadPicUrls()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TitleText(model.dialogTitle, modifier = Modifier.padding(bottom = 5.dp))
        if (model.isInitialized) {
            Row {
                NavigationButton(model::showPrevious, model.hasPreviousImage, Icons.AutoMirrored.Filled.ArrowBack)
                Box(modifier = Modifier.defaultMinSize(60.dp), contentAlignment = Alignment.Center) {
                    Text("${model.currentImageIndex + 1} / ${model.totalImageCount}")
                }
                NavigationButton(model::showNext, model.hasNextImage, Icons.AutoMirrored.Filled.ArrowForward)
            }
        }
        Box(Modifier.fillMaxSize()) {
            if (model.isInitialized && model.currentImageData != null) {
                androidx.compose.foundation.Image(
                    bitmap = model.currentImageData!!,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().align(Alignment.Center),
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun NavigationButton(onClick: () -> Unit, isEnabled: Boolean, icon: ImageVector) {
    Button(
        onClick = onClick, enabled = isEnabled, contentPadding = PaddingValues(0.dp),
        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp).size(20.dp)
    ) {
        Icon(icon, contentDescription = null)
    }
}
