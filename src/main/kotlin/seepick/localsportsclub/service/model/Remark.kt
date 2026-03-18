package seepick.localsportsclub.service.model

import androidx.compose.ui.graphics.Color
import seepick.localsportsclub.persistence.ActivityRemarkDbo
import seepick.localsportsclub.persistence.RemarkDboRating
import seepick.localsportsclub.persistence.TeacherRemarkDbo
import seepick.localsportsclub.view.common.darker

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
    Amazing("Amazing", "💖", 3, 3, Color.Green),
    Good("Good", "💚", 2, 2, Color.Green.darker().darker()),
    Meh("Meh", "😕", 1, -1, Color.Yellow),
    Bad("Bad", "❌", 0, -3, Color.Red),
    ;

    companion object {
        val default = Good
    }
}

fun ActivityRemarkDbo.toActivityRemark() = ActivityRemark(
    id = this.id,
    venueId = this.venueId,
    name = this.name,
    remark = this.remark,
    rating = this.rating.toRemarkRating(),
)

private fun RemarkDboRating.toRemarkRating() = when (this) {
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
