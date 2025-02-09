package seepick.localsportsclub.view.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.firstUpper
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.view.common.CheckboxTexted
import seepick.localsportsclub.view.common.ConditionalTooltip
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.preferences.tooltipTextVerifyUscFirst

@Composable
fun SubEntityDetail(
    subEntity: SubEntity,
    modifier: Modifier = Modifier,
    clock: Clock = koinInject(),
    onBook: (SubEntity) -> Unit,
    onCancelBooking: (SubEntity) -> Unit,
    isBookOrCancelPossible: Boolean,
    isBookingOrCancelInProgress: Boolean,
    onActivityChangeToCheckedin: (Activity) -> Unit,
    isGcalEnabled: Boolean,
    shouldGcalBeManaged: MutableState<Boolean>,
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
                    append("${Icons.Lsc.reservedEmoji} ${subEntity.bookedLabel.firstUpper()} ")
                }
                if (isCheckedin) {
                    "${Icons.Lsc.checkedinEmoji} checked-in "
                }
                if (isNoshow) {
                    Text("${Icons.Lsc.noshowEmoji} no-show")
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                ConditionalTooltip(
                    !isBookOrCancelPossible,
                    tooltipTextVerifyUscFirst,
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
                if (isBooked && subEntity is SubEntity.ActivityEntity && subEntity.activity.cancellationLimit != null) {
                    Text(
                        maxLines = 2,
                        text = "Free cancel until:\n${subEntity.activity.cancellationLimit.prettyPrint()}",
                        fontSize = 10.sp,
                    )
                }
                if (isGcalEnabled) {
                    CheckboxTexted(
                        label = "Manage Calendar",
                        checked = shouldGcalBeManaged,
                        enabled = isBookOrCancelPossible && !isBookingOrCancelInProgress,
                    )
                }
                AnimatedVisibility(
                    visible = isBookingOrCancelInProgress, enter = fadeIn(), exit = fadeOut()
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        if (subEntity is SubEntity.ActivityEntity) {
            if (subEntity.activity.state == ActivityState.Noshow ||
                subEntity.activity.state == ActivityState.CancelledLate
            ) {
                Button(onClick = { onActivityChangeToCheckedin(subEntity.activity) }) {
                    Text("Change to Checked-In")
                }
            }
        }
    }
}
