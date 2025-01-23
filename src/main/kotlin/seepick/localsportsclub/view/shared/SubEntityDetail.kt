package seepick.localsportsclub.view.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.firstUpper
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.view.common.ConditionalTooltip
import seepick.localsportsclub.view.common.Lsc
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
    isBookOrCancelPossible: Boolean,
    isBookingOrCancelInProgress: Boolean,
    onActivityNoshowToCheckedin: (Activity) -> Unit,
) {
    val year = clock.today().year
    val (isBooked, isCheckedin, isNoshow) = when (subEntity) {
        is SubEntity.ActivityEntity -> {
            Triple(
                subEntity.activity.state == ActivityState.Booked,
                subEntity.activity.state == ActivityState.Checkedin,
                subEntity.activity.state == ActivityState.Noshow
            )
        }

        is SubEntity.FreetrainingEntity -> {
            Triple(
                subEntity.freetraining.state == FreetrainingState.Scheduled,
                subEntity.freetraining.state == FreetrainingState.Checkedin,
                false
            )
        }
    }


    Column(modifier = modifier) {
        TitleText(subEntity.name)
        Text(subEntity.dateFormatted(year), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            text = buildString {
                if (isBooked) {
                    append("${Icons.Lsc.booked} ${subEntity.bookedLabel.firstUpper()} ")
                }
                if (isCheckedin) {
                    "${Icons.Lsc.checkedin} checked-in "
                }
                if (isNoshow) {
                    Text("${Icons.Lsc.noshow} no-show")
                }
                append(subEntity.category)
                if (subEntity is SubEntity.ActivityEntity) {
                    subEntity.activity.teacher?.also { append(" with $it") }
                }
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (subEntity.date >= clock.today()) {
            Row {
                ConditionalTooltip(
                    !isBookOrCancelPossible,
                    "Please enter your USC credentials in the preferences first."
                ) {
                    Button(
                        onClick = {
                            if (isBooked) {
                                onCancelBooking(subEntity)
                            } else {
                                onBook(subEntity)
                            }
                        },
                        enabled = isBookOrCancelPossible && !isBookingOrCancelInProgress
                    ) {
                        Text(if (isBooked) "Cancel ${subEntity.bookLabel}ing" else subEntity.bookLabel)
                    }
                }
                AnimatedVisibility(
                    visible = isBookingOrCancelInProgress, enter = fadeIn(), exit = fadeOut()
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        if (subEntity is SubEntity.ActivityEntity) {
            if (subEntity.activity.state == ActivityState.Noshow) {
                Button(onClick = { onActivityNoshowToCheckedin(subEntity.activity) }) {
                    Text("Change noshow to checkedin")
                }
            }
        }
    }
}
