package seepick.localsportsclub.view.activity

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import seepick.localsportsclub.service.model.Activity

fun AnnotatedString.Builder.appendTeacher(activity: Activity) {
    if (activity.teacher == null) {
        return
    }
    val rating = activity.teacherRemarkRating
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
