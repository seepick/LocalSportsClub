package seepick.localsportsclub.service.model

import androidx.compose.ui.graphics.Color
import lsc.repo.ActivityRemarkDbo
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.remark.RemarkViewEntity
import seepick.localsportsclub.view.remark.RemarkViewType

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

fun ActivityRemarkDbo.toRemarkViewEntity() = RemarkViewEntity(
    id = this.id,
    type = RemarkViewType.WithVenue(this.venueId),
    name = this.name,
    remark = this.remark,
    rating = this.rating.toRemarkRating(),
)

data class TeacherRemark(
    override val id: Int,
    override val venueId: Int,
    override val name: String,
    override val remark: String,
    override val rating: RemarkRating,
) : Remark
