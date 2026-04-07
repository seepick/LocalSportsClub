package seepick.localsportsclub.view.shared

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.seepick.uscclient.plan.Plan
import seepick.localsportsclub.service.SortDirection
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import seepick.localsportsclub.service.model.HasCategory
import seepick.localsportsclub.service.model.HasDistance
import seepick.localsportsclub.service.model.HasPlan
import seepick.localsportsclub.service.model.HasScore
import seepick.localsportsclub.service.model.HasVenue
import seepick.localsportsclub.view.DistanceIndicator
import seepick.localsportsclub.view.common.LscIcons
import seepick.localsportsclub.view.common.ModifierWith
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.CellValue
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.TableTextCell

fun <T : HasVenue> CheckedinColumn(paddingRight: Boolean = false) = TableColumn<T>(
    header = VisualIndicator.EmojiIndicator(LscIcons.checkedinEmoji),
    size = WidthOrWeight.Width(40.dp),
    renderer = CellRenderer.TextRenderer.forInt(
        textAlign = TextAlign.Right,
        paddingRight = paddingRight,
        extractor = {
            it.venue.activities.filter { it.state == ActivityState.Checkedin }.size +
                    it.venue.freetrainings.filter { it.state == FreetrainingState.Checkedin }.size
        },
    ),
    tooltip = "Check-ins",
    initialSortDirection = SortDirection.Desc,
)

fun <T : HasVenue> RatingColumn(paddingRight: Boolean = false) = TableColumn<T>(
    header = VisualIndicator.StringIndicator("Rating"),
    size = WidthOrWeight.Width(90.dp + if (paddingRight) 8.dp else 0.dp),
    renderer = CellRenderer.TextRenderer(
        textAlign = TextAlign.Center,
        paddingRight = paddingRight,
        valueExtractor = { CellValue(it.venue.rating.label) },
        sortExtractor = { it.venue.rating.value },
    ),
    initialSortDirection = SortDirection.Desc,
)

fun <T : HasDistance> DistanceColumn() = TableColumn<T>(
    header = VisualIndicator.StringIndicator("km"),
    size = WidthOrWeight.Width(35.dp),
    sortValueExtractor = { it.distanceInKm },
    renderer = CellRenderer.CustomRenderer { item, col ->
        Row(
            modifier = ModifierWith(col.size).height(30.dp),
        ) {
            DistanceIndicator(item)
        }
    })

fun <T : HasPlan> PlanColumn() = TableColumn<T>(
    header = VisualIndicator.StringIndicator(Plan.UscPlan.Large.emoji),
    size = WidthOrWeight.Width(40.dp),
    tooltip = "Plan: ${Plan.UscPlan.entries.joinToString(" ") { "${it.emoji} ${it.apiString}" }}",
    renderer = CellRenderer.TextRenderer(
        sortExtractor = { it.plan.id },
        valueExtractor = { CellValue(it.plan.emoji) },
        textAlign = TextAlign.Center,
    )
)

fun <T : HasScore> ScoreColumn() = TableColumn<T>(
    header = VisualIndicator.StringIndicator("Score"),
    size = WidthOrWeight.Width(100.dp),
    renderer = CellRenderer.TextRenderer(
        valueExtractor = { CellValue(it.score.toString()) },
        sortExtractor = { it.score },
        textAlign = TextAlign.Right,
    )
)

fun <T : HasCategory> CategoryColumn() = TableColumn<T>(
    VisualIndicator.StringIndicator("Category"),
    WidthOrWeight.Width(80.dp),
    CellRenderer.TextRenderer(
        valueExtractor = {
            CellValue(buildAnnotatedString {
                withStyle(SpanStyle(color = it.category.rating?.color ?: Color.Unspecified)) {
                    it.category.rating?.emoji?.let { append("$it ") }
                    append(it.category.nameAndMaybeEmoji)
                }
            })
        },
        sortExtractor = { it.category.name.lowercase() },
    )
)

fun <T : HasVenue> VenueColumn(
    headerLabel: String,
    size: WidthOrWeight = WidthOrWeight.Weight(0.4f),
    paddingLeft: Boolean = false,
) = TableColumn<T>(
    header = VisualIndicator.StringIndicator(headerLabel),
    size = size,
    sortValueExtractor = { it.venue.name.lowercase() },
    renderer = CellRenderer.CustomRenderer { venueHaving, column ->
        TableTextCell(
            value = CellValue(venueHaving.venue.nameAndFavWishEmojiPrefixedAnnotated),
            size = column.size,
            textDecoration = if (venueHaving.venue.isDeleted) TextDecoration.LineThrough else null,
            paddingLeft = paddingLeft,
        )
    },
)
