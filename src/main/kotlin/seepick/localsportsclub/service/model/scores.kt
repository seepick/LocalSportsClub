package seepick.localsportsclub.service.model

import androidx.compose.ui.graphics.Color
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.LscColors
import kotlin.math.roundToInt

/** 0.0 (red) to 1.0 (green); 0.5 = neutral */
typealias Score = Double

@Suppress("UnusedReceiverParameter")
fun LscColors.forScore(score: Score?, venue: Venue): Color? =
    if (venue.isWishlisted) {
        Lsc.colors.wishlistedBgColor
    } else {
        score?.let { nonNullScore ->
            Lsc.colors.forTableBg(
                score = nonNullScore,
                saturation = if (venue.isFavorited && nonNullScore > 0.7) 1.0 else 0.6,
            )
        }
    }

val RemarkRating.scoreModifier: Double
    get() = when (this) {
        RemarkRating.Amazing -> 0.2
        RemarkRating.Good -> 0.08
        RemarkRating.Meh -> -0.1
        RemarkRating.Bad -> -100.0
    }

val RemarkRating.categoryScoreModifier: Double
    get() = when (this) {
        RemarkRating.Amazing -> 0.08
        RemarkRating.Good -> 0.05
        RemarkRating.Meh -> -0.06
        RemarkRating.Bad -> -0.15
    }

val Rating.scoreModiferForActivity: Double
    get() = when (this) {
        Rating.R0 -> 0.0
        Rating.R1 -> -0.25
        Rating.R2 -> -0.15
        Rating.R3 -> 0.05
        Rating.R4 -> 0.14
        Rating.R5 -> 0.2
    }
val Rating.scoreModiferForVenue: Double
    get() = when (this) {
        Rating.R0 -> 0.0
        Rating.R1 -> -0.4
        Rating.R2 -> -0.2
        Rating.R3 -> 0.05
        Rating.R4 -> 0.15
        Rating.R5 -> 0.25
    }

fun Venue.calcScore(): Score? {
    if (!isAnyScoreRelevantSet()) {
        return null
    }
    var score = 0.5
    score += venue.categories.mapNotNull { it.rating }.fold(0.0) { acc, rating ->
        acc + rating.categoryScoreModifier
    }
    if (venue.isFavorited) score += 0.25
    if (venue.isWishlisted) score += 0.15
    score += venue.rating.scoreModiferForVenue
    return ((score.coerceIn(0.0..1.0) * 100).roundToInt()) / 100.0
}

fun Activity.calcScore(): Score? {
    if (!isAnyScoreRelevantSet()) {
        return null
    }
    var score = 0.5
    category.rating?.categoryScoreModifier?.let {
        score += it * 2.0
    }
    if (venue.isFavorited) score += 0.20
    if (venue.isWishlisted) score += 0.15
    score += venue.rating.scoreModiferForActivity
    score += remark?.rating?.scoreModifier ?: 0.0
    score += teacherRemark?.rating?.scoreModifier ?: 0.0
    val finalScore = ((score.coerceIn(0.0..1.0) * 100).roundToInt()) / 100.0
    return if (finalScore in 0.49..0.51) null else finalScore // drop insignificants
}

private fun Venue.isAnyScoreRelevantSet() =
    venue.isFavorited || venue.isWishlisted || venue.rating != Rating.R0 || venue.categories.any { it.rating != null }

private fun Activity.isAnyScoreRelevantSet() =
    venue.isAnyScoreRelevantSet() || remark != null || teacherRemark != null || category.rating != null
