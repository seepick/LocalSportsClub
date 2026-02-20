package seepick.localsportsclub.view.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.firstUpper
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.view.common.CheckboxTexted
import seepick.localsportsclub.view.common.ConditionalTooltip
import seepick.localsportsclub.view.common.CustomDialog
import seepick.localsportsclub.view.common.LongText
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.preferences.tooltipTextVerifyUscFirst

@Composable
fun SubEntityDetail(
    subEntity: SubEntity,
    isSyncButtonVisible: Boolean,
    isBookOrCancelPossible: Boolean,
    isBookingOrCancelInProgress: Boolean,
    isGcalEnabled: Boolean,
    isGcalManaged: MutableState<Boolean>,
    onBook: (SubEntity) -> Unit,
    onCancelBooking: (SubEntity) -> Unit,
    isSyncActivityInProgress: Boolean,
    onSyncActivity: () -> Unit,
    onActivityChangeToCheckedin: (Activity) -> Unit,
    clock: Clock = koinInject(),
    sharedModel: SharedModel = koinInject(),
    modifier: Modifier = Modifier,
) {
    val year = clock.today().year
    val (isBooked, isCheckedin, isNoshow) = extractStatesOf(subEntity)

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val syncSize = 30
            Box(contentAlignment = Alignment.Center, modifier = Modifier.width(syncSize.dp).height(syncSize.dp)) {
                if (isSyncButtonVisible) {
                    if (isSyncActivityInProgress) {
                        val progressSize = syncSize - 10
                        CircularProgressIndicator(
                            modifier = Modifier.width(progressSize.dp).height(progressSize.dp)
                        )
                    } else {
                        Tooltip("Sync details (description, teacher) of this activity") {
                            TextButton(onClick = onSyncActivity) {
                                Icon(
                                    Lsc.icons.syncActivityDetails, contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
            Tooltip(subEntity.name) {
                TitleText(subEntity.name)
            }
        }
        Row {
            Text(subEntity.dateFormatted(year), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subEntity is SubEntity.ActivityEntity) {
                Tooltip("Plan ${subEntity.activity.plan.label} (${subEntity.activity.plan.apiString})") {
                    Text(" ${subEntity.activity.plan.emoji} ")
                }
            }
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
                        append(" (${subEntity.activity.spotsLeft} spots)")
                    }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (subEntity is SubEntity.ActivityEntity && subEntity.activity.description != null) {
            val description = subEntity.activity.description!!
//            Text(text = subEntity.activity.description!!)
            LongText(label = "Info", text = description, onShowLongText = {
                sharedModel.customDialog.value =
                    CustomDialog(title = "Activity Description", text = description, showDismissButton = false)
            })
        }
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
                        }, enabled = isBookOrCancelPossible && !isBookingOrCancelInProgress
                    ) {
                        Text(if (isBooked) "Cancel ${subEntity.bookLabel}ing" else subEntity.bookLabel)
                    }
                }
                if (isGcalEnabled) {
                    CheckboxTexted(
                        label = "Manage Calendar",
                        checked = isGcalManaged,
                        enabled = isBookOrCancelPossible && !isBookingOrCancelInProgress,
                    )
                }
                if (subEntity is SubEntity.ActivityEntity) {
                    val cancellationLimit = subEntity.activity.cancellationLimit
                    if (cancellationLimit != null) {
                        Text(
                            maxLines = 2,
                            text = "Cancel until:\n${cancellationLimit.prettyPrint(clock.today().year)}",
                            fontSize = 10.sp,
                            color = if (clock.now() > cancellationLimit) Color.Red else Color.Green,
                        )
                    }
                }
                AnimatedVisibility(visible = isBookingOrCancelInProgress, enter = fadeIn(), exit = fadeOut()) {
                    CircularProgressIndicator()
                }
            }
        }
        if (subEntity is SubEntity.ActivityEntity) {
            if (subEntity.activity.state == ActivityState.Noshow || subEntity.activity.state == ActivityState.CancelledLate) {
                Button(onClick = { onActivityChangeToCheckedin(subEntity.activity) }) {
                    Text("Change to Checked-In")
                }
            }
        }
    }
}

private fun extractStatesOf(subEntity: SubEntity) = when (subEntity) {
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
