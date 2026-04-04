package seepick.localsportsclub.view.shared

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.firstUpper
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.activity.appendRatedTeacher
import seepick.localsportsclub.view.common.CheckboxTexted
import seepick.localsportsclub.view.common.CustomDialog
import seepick.localsportsclub.view.common.LongText
import seepick.localsportsclub.view.common.Lsc
import seepick.localsportsclub.view.common.TitleText
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.preferences.tooltipTextVerifyUscFirst
import seepick.localsportsclub.view.remark.withColor

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
    val (isBooked, isCheckedin, isNoshow, isCancelledLate) = extractStatesOf(subEntity)

    Column(modifier = modifier) {
        val remarkRatingEmoji = subEntity.maybeActivity?.remark?.rating?.let { "${it.emoji} " } ?: ""
        TitleText(remarkRatingEmoji + subEntity.name)

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSyncButtonVisible) {
                val syncSize = 30
                Box(contentAlignment = Alignment.Center, modifier = Modifier.width(syncSize.dp).height(syncSize.dp)) {
                    if (isSyncActivityInProgress) {
                        val progressSize = syncSize - 10
                        CircularProgressIndicator(
                            modifier = Modifier.width(progressSize.dp).height(progressSize.dp)
                        )
                    } else {
                        Tooltip("Sync details (description, teacher) of this activity") {
                            TextButton(onClick = onSyncActivity) {
                                Icon(
                                    Lsc.icons.manualSync, contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
            SelectionContainer {
                Text(
                    subEntity.dateFormatted(year),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 3.dp)
                )
            }
            if (subEntity is SubEntity.ActivityEntity) {
                Tooltip("${subEntity.activity.plan.emoji} Plan ${subEntity.activity.plan.label} (${subEntity.activity.plan.apiString})") {
                    SelectionContainer {
                        Text(" ${subEntity.activity.plan.emoji} ")
                    }
                }
            }
            SelectionContainer {
                Text(
                    text = buildAnnotatedString {
                        /*
                        also use color for checkedin/booked/cancel&noshow
                         */
                        if (isBooked) {
                            withStyle(SpanStyle(color = Lsc.colors.activityBooked)) {
                                append("${Icons.Lsc.reservedEmoji} ${subEntity.bookedLabel.firstUpper()} ")
                            }
                        }
                        if (isCheckedin) {
                            withStyle(SpanStyle(color = Lsc.colors.activityCheckedin)) {
                                append("${Icons.Lsc.checkedinEmoji} checked-in ")
                            }
                        }
                        if (isNoshow) {
                            withStyle(SpanStyle(color = Lsc.colors.activityNoShow)) {
                                append("${Icons.Lsc.noshowEmoji} no-show ")
                            }
                        }
                        if (isCancelledLate) {
                            withStyle(SpanStyle(color = Lsc.colors.activityCancelledLate)) {
                                append("${Icons.Lsc.cancelledLateEmoji} cancelled-late ")
                            }
                        }
                        withColor(subEntity.category.rating) {
                            append(subEntity.category.nameAndMaybeEmoji)
                        }

                        if (subEntity is SubEntity.ActivityEntity) {
                            appendRatedTeacher(subEntity.activity)
                            append(" (${subEntity.activity.spotsLeft} spots)")
                        }
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (subEntity is SubEntity.ActivityEntity) {
            val description = subEntity.activity.description
            if (!description.isNullOrEmpty()) {
                LongText(
                    text = description,
                    maxLines = 1,
                    onShowLongText = {
                        sharedModel.customDialog.value =
                            CustomDialog(
                                title = subEntity.activity.name,
                                content = { Text(description) },
                                showDismissButton = false,
                            )
                    },
                )
            }

        }
        if (subEntity.date >= clock.today()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Tooltip(if (!isBookOrCancelPossible) tooltipTextVerifyUscFirst else null) {
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
                            color = if (clock.now() <= cancellationLimit) Lsc.colors.cancelBookingWithin else Lsc.colors.cancelBookingOutside,
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

data class SubEntityStates(
    val isBooked: Boolean,
    val isCheckedIn: Boolean,
    val isNoshow: Boolean,
    val isCancelledLate: Boolean,
)

private fun extractStatesOf(subEntity: SubEntity) = when (subEntity) {
    is SubEntity.ActivityEntity -> {
        SubEntityStates(
            subEntity.activity.state == ActivityState.Booked,
            subEntity.activity.state == ActivityState.Checkedin,
            subEntity.activity.state == ActivityState.Noshow,
            subEntity.activity.state == ActivityState.CancelledLate,
        )
    }

    is SubEntity.FreetrainingEntity -> {
        SubEntityStates(
            subEntity.freetraining.state == FreetrainingState.Scheduled,
            subEntity.freetraining.state == FreetrainingState.Checkedin,
            false,
            false,
        )
    }
}

