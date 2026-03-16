package seepick.localsportsclub.service.model

import seepick.localsportsclub.persistence.ActivityRemarkDbo
import seepick.localsportsclub.persistence.ActivityRemarkDboRating
import seepick.localsportsclub.persistence.TeacherRemarkDbo
import seepick.localsportsclub.persistence.TeacherRemarkDboRating

interface Remark {
    val id: Int
    val venueId: Int
    val name: String
    val remark: String
}

data class ActivityRemark(
    override val id: Int,
    override val venueId: Int,
    override val name: String,
    override val remark: String,
    val rating: ActivityRemarkRating,
) : Remark

enum class ActivityRemarkRating(
    val label: String,
    val emoji: String,
) {
    Amazing("Amazing", "💖"),
    Good("Good", "💚"),
    Meh("Meh", "😕"),
    Bad("Bad", "❌"),
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
    rating = this.rating.toActivityRemarkRating(),
)

private fun ActivityRemarkDboRating.toActivityRemarkRating() = when (this) {
    ActivityRemarkDboRating.Amazing -> ActivityRemarkRating.Amazing
    ActivityRemarkDboRating.Good -> ActivityRemarkRating.Good
    ActivityRemarkDboRating.Meh -> ActivityRemarkRating.Meh
    ActivityRemarkDboRating.Bad -> ActivityRemarkRating.Bad
}

data class TeacherRemark(
    override val id: Int,
    override val venueId: Int,
    override val name: String,
    override val remark: String,
    val rating: TeacherRemarkRating,
) : Remark

enum class TeacherRemarkRating(
    val label: String,
    val emoji: String,
) {
    Amazing("Amazing", "💖"),
    Good("Good", "💚"),
    Meh("Meh", "😕"),
    Bad("Bad", "❌"),
    ;

    companion object {
        val default = Good
    }
}

fun TeacherRemarkDbo.toTeacherRemark() = TeacherRemark(
    id = this.id,
    venueId = this.venueId,
    name = this.name,
    remark = this.remark,
    rating = this.rating.toTeacherRemarkRating(),
)

private fun TeacherRemarkDboRating.toTeacherRemarkRating() = when (this) {
    TeacherRemarkDboRating.Amazing -> TeacherRemarkRating.Amazing
    TeacherRemarkDboRating.Good -> TeacherRemarkRating.Good
    TeacherRemarkDboRating.Meh -> TeacherRemarkRating.Meh
    TeacherRemarkDboRating.Bad -> TeacherRemarkRating.Bad
}
