package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.ActivityRemarkRepo
import seepick.localsportsclub.persistence.TeacherRemarkRepo
import seepick.localsportsclub.service.model.toRemarkViewEntity
import seepick.localsportsclub.view.remark.RemarkViewEntity
import seepick.localsportsclub.view.remark.RemarkViewType

class RemarkService(
    private val activityRemarkRepo: ActivityRemarkRepo,
    private val teacherRemarkRepo: TeacherRemarkRepo,
) {
    fun selectAll() = Remarks(
        activityRemarksByVenueId = activityRemarkRepo
            .selectAll()
            .map { it.toRemarkViewEntity() }
            .groupBy { (it.type as RemarkViewType.WithVenue).venueId },
        teacherRemarksByVenueId = teacherRemarkRepo
            .selectAll()
            .map { it.toRemarkViewEntity() }
            .groupBy { (it.type as RemarkViewType.WithVenue).venueId },
    )
}

class Remarks(
    private val activityRemarksByVenueId: Map<Int, List<RemarkViewEntity>>,
    private val teacherRemarksByVenueId: Map<Int, List<RemarkViewEntity>>,
) {
    fun forActivities(venueId: Int): List<RemarkViewEntity> =
        activityRemarksByVenueId[venueId] ?: emptyList()

    fun forTeachers(venueId: Int): List<RemarkViewEntity> =
        teacherRemarksByVenueId[venueId] ?: emptyList()
}
