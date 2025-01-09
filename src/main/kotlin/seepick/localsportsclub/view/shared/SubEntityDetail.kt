package seepick.localsportsclub.view.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.TitleText

data class BookingDialog(
    val title: String,
    val message: String,
)

@Composable
fun SubEntityDetail(
    subEntity: SubEntity,
    modifier: Modifier = Modifier,
    clock: Clock = koinInject(),
    onBook: (SubEntity) -> Unit,
    onCancelBooking: (SubEntity) -> Unit,
    isBookingOrCancelInProgress: Boolean,
    bookingDialog: BookingDialog?,
    onCloseDialog: () -> Unit,
) {
    if (bookingDialog != null) {
        AlertDialog(
            title = { Text(bookingDialog.title) },
            text = { Text(bookingDialog.message) },
            onDismissRequest = onCloseDialog,
            backgroundColor = MaterialTheme.colors.background,
            confirmButton = {
                Button(onClick = onCloseDialog) {
                    Text("Close")
                }
            }
        )
    }

    val year = clock.today().year
    Column(modifier = modifier) {
        TitleText(subEntity.name)
        Text("Date: ${subEntity.dateFormatted(year)}")
        Text("Category: ${subEntity.category}")
        if (subEntity is SubEntity.ActivityEntity) {
            Text("Teacher: ${subEntity.activity.teacher ?: "-"}")
            Text("Spots Left: ${subEntity.activity.spotsLeft}")
        }
        if (subEntity.isBooked) {
            Text("${Icons.Lsc.booked} Is ${subEntity.bookedLabel}")
        }
        if (subEntity.wasCheckedin) {
            Text("${Icons.Lsc.checkedin} Was checked-in")
        }
        Row {
            Button(onClick = {
                if (subEntity.isBooked) {
                    onCancelBooking(subEntity)
                } else {
                    onBook(subEntity)
                }
            }, enabled = !isBookingOrCancelInProgress) {
                Text(if (subEntity.isBooked) "Cancel ${subEntity.bookLabel}ing" else subEntity.bookLabel)
            }
            AnimatedVisibility(
                visible = isBookingOrCancelInProgress, enter = fadeIn(), exit = fadeOut()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
