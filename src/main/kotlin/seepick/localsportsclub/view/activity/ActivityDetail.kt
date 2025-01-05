package seepick.localsportsclub.view.activity

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
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.TitleText

data class BookingDialog(
    val title: String,
    val message: String,
)

@Composable
fun ActivityDetail(
    activity: Activity,
    modifier: Modifier = Modifier,
    clock: Clock = koinInject(),
    onBook: (Activity) -> Unit,
    onCancelBooking: (Activity) -> Unit,
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
        TitleText(activity.name)
        Text("Date: ${activity.dateTimeRange.prettyPrint(year)}")
        Text("Category: ${activity.category}")
        Text("Teacher: ${activity.teacher ?: "-"}")
        Text("Spots Left: ${activity.spotsLeft}")
        if (activity.isBooked) {
            Text("${Icons.Lsc.booked} Is booked")
        }
        if (activity.wasCheckedin) {
            Text("${Icons.Lsc.checkedin} Was checked-in")
        }
        Row {
            Button(onClick = {
                if (activity.isBooked) {
                    onCancelBooking(activity)
                } else {
                    onBook(activity)
                }
            }, enabled = !isBookingOrCancelInProgress) {
                Text(if (activity.isBooked) "Cancel Booking" else "Book")
            }
            AnimatedVisibility(
                visible = isBookingOrCancelInProgress, enter = fadeIn(), exit = fadeOut()
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
