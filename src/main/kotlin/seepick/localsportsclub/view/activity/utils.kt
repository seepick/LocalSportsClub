package seepick.localsportsclub.view.activity

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.view.remark.withColor

fun AnnotatedString.Builder.appendRatedName(activity: Activity) {
    val remarkRating = activity.remark?.rating
    if (remarkRating != null) {
        append("${remarkRating.emoji} ")
    }
    withColor(remarkRating) {
        append(activity.name)
    }
}

fun AnnotatedString.Builder.appendRatedTeacher(activity: Activity) {
    if (activity.teacher == null) {
        return
    }
    val rating = activity.teacherRemark?.rating
    val teacherString = " /${activity.teacher}"
    if (rating == null) {
        append(teacherString)
        return
    }
    withStyle(style = SpanStyle(color = rating.color)) {
        append(teacherString)
    }
    append(" ${rating.emoji}")

}
