package seepick.localsportsclub.service

import seepick.localsportsclub.service.model.GlobalRemarkType
import seepick.localsportsclub.view.remark.RemarkViewEntity

class GlobalRemarkFinder(
    private val globalRemarkService: GlobalRemarkService,
) {
    private val activityRemarks by lazy {
        globalRemarkService.selectAll(GlobalRemarkType.Activity)
    }

    private val teacherRemarks by lazy {
        globalRemarkService.selectAll(GlobalRemarkType.Teacher)
    }

    fun findForActivity(name: String): RemarkViewEntity? =
        activityRemarks.findMatchingRemark(name)

    fun findForTeacher(teacher: String?): RemarkViewEntity? =
        teacherRemarks.findMatchingRemark(teacher)
}

fun List<RemarkViewEntity>.findMatchingRemark(name: String?): RemarkViewEntity? {
    if (name == null) {
        return null
    }
    return this.filter { name.contains(it.name.value, ignoreCase = true) }.let {
        when (it.size) {
            0 -> null
            1 -> it.single()
            else -> {
                it.maxBy { it.name.value.length }
            }
        }
    }
}
