package seepick.localsportsclub.service.model

import androidx.compose.ui.graphics.Color
import seepick.localsportsclub.persistence.ActivityRemarkDbo
import seepick.localsportsclub.persistence.RemarkDboRating
import seepick.localsportsclub.persistence.TeacherRemarkDbo
import seepick.localsportsclub.view.Lsc

interface Remark {
    val id: Int
    val venueId: Int
    val name: String
    val remark: String
    val rating: RemarkRating
}

data class ActivityRemark(
    override val id: Int,
    override val venueId: Int,
    override val name: String,
    override val remark: String,
    override val rating: RemarkRating,
) : Remark

enum class RemarkRating(
    val label: String,
    val emoji: String,
    val numericValue: Int,
    val weightedValue: Int,
    val color: Color,
) {
    Amazing("Amazing", "💚", 3, 3, Lsc.colors.remarkRatingAmazing),
    Good("Good", "☘️", 2, 2, Lsc.colors.remarkRatingGood),
    Meh("Meh", "😕", 1, -1, Lsc.colors.remarkRatingMeh),
    Bad("Bad", "❌", 0, -3, Lsc.colors.remarkRatingBad),
    ;

    companion object {
        val default = Good
    }
}

fun RemarkRating.toRemarkDboRating() = when (this) {
    RemarkRating.Amazing -> RemarkDboRating.Amazing
    RemarkRating.Good -> RemarkDboRating.Good
    RemarkRating.Meh -> RemarkDboRating.Meh
    RemarkRating.Bad -> RemarkDboRating.Bad
}

fun ActivityRemarkDbo.toActivityRemark() = ActivityRemark(
    id = this.id,
    venueId = this.venueId,
    name = this.name,
    remark = this.remark,
    rating = this.rating.toRemarkRating(),
)

fun RemarkDboRating.toRemarkRating() = when (this) {
    RemarkDboRating.Amazing -> RemarkRating.Amazing
    RemarkDboRating.Good -> RemarkRating.Good
    RemarkDboRating.Meh -> RemarkRating.Meh
    RemarkDboRating.Bad -> RemarkRating.Bad
}

data class TeacherRemark(
    override val id: Int,
    override val venueId: Int,
    override val name: String,
    override val remark: String,
    override val rating: RemarkRating,
) : Remark

fun TeacherRemarkDbo.toTeacherRemark() = TeacherRemark(
    id = this.id,
    venueId = this.venueId,
    name = this.name,
    remark = this.remark,
    rating = this.rating.toRemarkRating(),
)
