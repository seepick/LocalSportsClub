package seepick.localsportsclub.view.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.TitleText

@Composable
fun ActivityDetail(
    activity: Activity,
    modifier: Modifier = Modifier,
    clock: Clock = koinInject(),
) {
    val year = clock.today().year
    Column(modifier = modifier) {
        TitleText(activity.name)
        Text("Date: ${activity.dateTimeRange.prettyPrint(year)}")
        Text("Category: ${activity.category}")
        Text("Teacher: ${activity.teacher ?: "-"}")
        Text("Spots Left: ${activity.spotsLeft}") // TODO could sync only spots for subsequent syncs...
        if (activity.isBooked) {
            Text("${Icons.Lsc.booked} Is booked")
        }
        if (activity.wasCheckedin) {
            Text("${Icons.Lsc.checkedin} Was checked-in")
        }
    }

}
