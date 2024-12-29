package seepick.localsportsclub.view.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import seepick.localsportsclub.service.Clock
import seepick.localsportsclub.service.model.Activity
import seepick.localsportsclub.service.prettyPrint
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.TitleText

@Composable
fun ActivityDetail(
    activity: Activity?,
    modifier: Modifier = Modifier,
    clock: Clock = koinInject(),
) {
    val year = clock.today().year
    Column(modifier = modifier) {
        TitleText(activity?.name ?: "N/A")
        Text("Category: ${activity?.category ?: ""}")
        Text("Time: ${activity?.dateTimeRange?.prettyPrint(year) ?: ""}")
        Text("Spots Left: ${activity?.spotsLeft ?: "-"}")
        if (activity?.isBooked == true) {
            Text("${Icons.Lsc.booked} Is booked")
        }
        if (activity?.wasCheckedin == true) {
            Text("${Icons.Lsc.checkedin} Was checked-in")
        }
    }

}
