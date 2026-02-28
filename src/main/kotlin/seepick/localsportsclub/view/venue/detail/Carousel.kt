package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import com.github.seepick.uscclient.UscApi
import org.koin.compose.koinInject
import seepick.localsportsclub.service.FileResolver
import seepick.localsportsclub.service.model.Venue
import seepick.localsportsclub.view.shared.SharedModel

class CarouselViewModel(
    private val shared: SharedModel,
    private val api: UscApi,
    private val fileResolver: FileResolver,
) : ViewModel() {

    var isLoading = mutableStateOf(true)
        private set

    fun onVenueDetailImageClicked(venue: Venue) {
        shared.carouselVenue.value = venue
    }

    fun dismiss() {
        shared.carouselVenue.value = null
    }
}

@Composable
fun CarouselDialog(
    model: CarouselViewModel = koinInject(),
    shared: SharedModel = koinInject(),
) {
    Dialog(onDismissRequest = model::dismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "Venue: ${shared.carouselVenue.value?.name}",
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}
