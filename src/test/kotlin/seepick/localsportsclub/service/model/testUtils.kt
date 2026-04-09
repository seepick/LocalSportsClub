package seepick.localsportsclub.service.model

import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import seepick.localsportsclub.view.remark.RemarkViewEntity

fun ActivityState.someOther() = ActivityState.entries.toSet().minus(this).random()

fun Activity.Companion.build(code: ActivityBuilder.() -> Unit): Activity =
    ActivityBuilder().apply(code).build()

class ActivityBuilder {

    var rating = Rating.R0
    var isFavorited = false
    var isWishlisted = false
    var name: String = "test activity"
    var teacher: String? = null
    var state = ActivityState.Blank
    var category = Category("category", null)

    val activityRemarks = mutableListOf<RemarkViewEntity>()
    val teacherRemarks = mutableListOf<RemarkViewEntity>()

    fun build(): Activity {
        val venue = Arb.venue().next().copy(
            rating = rating,
            isFavorited = isFavorited,
            isWishlisted = isWishlisted,
        ).also { venue ->
            venue.activityRemarks += this.activityRemarks
            venue.teacherRemarks += this.teacherRemarks
        }
        return Arb.activity().next().copy(
            name = name,
            teacher = teacher,
            state = state,
            category = category,
            venue = venue,
        )
    }
}
