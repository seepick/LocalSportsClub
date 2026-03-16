package seepick.localsportsclub.service.model

import seepick.localsportsclub.persistence.ActivityRemarkRepo
import seepick.localsportsclub.persistence.TeacherRemarkRepo

class Remarks(
    private val activityRemarksByVenueId: Map<Int, List<ActivityRemark>>,
    private val teacherRemarksByVenueId: Map<Int, List<TeacherRemark>>,
) {
    fun forActivities(venueId: Int): List<ActivityRemark> =
        activityRemarksByVenueId[venueId] ?: emptyList()

    fun forTeachers(venueId: Int): List<TeacherRemark> =
        teacherRemarksByVenueId[venueId] ?: emptyList()
}

class RemarkService(
    private val activityRemarkRepo: ActivityRemarkRepo,
    private val teacherRemarkRepo: TeacherRemarkRepo,
) {
    fun selectAll(): Remarks {
        return Remarks(
            activityRemarksByVenueId = activityRemarkRepo.selectAll().map { it.toActivityRemark() }
                .groupBy { it.venueId },
            teacherRemarksByVenueId = teacherRemarkRepo.selectAll().map { it.toTeacherRemark() }
                .groupBy { it.venueId },
        )
    }
}
