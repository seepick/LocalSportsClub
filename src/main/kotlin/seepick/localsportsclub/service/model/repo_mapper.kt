package seepick.localsportsclub.service.model

import com.github.seepick.uscclient.venue.VisitLimits
import lsc.repo.ActivityRemarkDbo
import lsc.repo.ActivityStateDbo
import lsc.repo.FreetrainingStateDbo
import lsc.repo.GlobalRemarkTypeDbo
import lsc.repo.RemarkDboRating
import lsc.repo.TeacherRemarkDbo
import lsc.repo.VisitLimitsDbo
import seepick.localsportsclub.view.remark.RemarkViewEntity
import seepick.localsportsclub.view.remark.RemarkViewType

fun TeacherRemarkDbo.toTeacherRemark() = TeacherRemark(
    id = this.id,
    venueId = this.venueId,
    name = this.name,
    remark = this.remark,
    rating = this.rating.toRemarkRating(),
)

fun TeacherRemarkDbo.toRemarkViewEntity() = RemarkViewEntity(
    id = this.id,
    type = RemarkViewType.WithVenue(this.venueId),
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

internal fun ActivityStateDbo.toActivityState() = when (this) {
    ActivityStateDbo.Blank -> ActivityState.Blank
    ActivityStateDbo.Booked -> ActivityState.Booked
    ActivityStateDbo.Checkedin -> ActivityState.Checkedin
    ActivityStateDbo.Noshow -> ActivityState.Noshow
    ActivityStateDbo.CancelledLate -> ActivityState.CancelledLate
}

internal fun FreetrainingStateDbo.toFreetrainingState() = when (this) {
    FreetrainingStateDbo.Blank -> FreetrainingState.Blank
    FreetrainingStateDbo.Scheduled -> FreetrainingState.Scheduled
    FreetrainingStateDbo.Checkedin -> FreetrainingState.Checkedin
}

internal fun GlobalRemarkTypeDbo.toGlobalRemarkType() = when (this) {
    GlobalRemarkTypeDbo.Category -> GlobalRemarkType.Category
    GlobalRemarkTypeDbo.Activity -> GlobalRemarkType.Activity
    GlobalRemarkTypeDbo.Teacher -> GlobalRemarkType.Teacher
}

internal fun GlobalRemarkType.toGlobalRemarkTypeDbo() = when (this) {
    GlobalRemarkType.Category -> GlobalRemarkTypeDbo.Category
    GlobalRemarkType.Activity -> GlobalRemarkTypeDbo.Activity
    GlobalRemarkType.Teacher -> GlobalRemarkTypeDbo.Teacher
}

internal fun VisitLimitsDbo.toVisitLimits() = VisitLimits(
    small = small,
    medium = medium,
    large = large,
    xlarge = xlarge,
)

internal fun VisitLimits.toVisitLimits() = VisitLimitsDbo(
    small = small,
    medium = medium,
    large = large,
    xlarge = xlarge,
)
