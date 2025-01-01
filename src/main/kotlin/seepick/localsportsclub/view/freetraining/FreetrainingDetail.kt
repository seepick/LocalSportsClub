package seepick.localsportsclub.view.freetraining

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import seepick.localsportsclub.service.date.Clock
import seepick.localsportsclub.service.date.prettyPrint
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.TitleText

@Composable
fun FreetrainingDetail(
    freetraining: Freetraining,
    modifier: Modifier = Modifier,
    clock: Clock = koinInject(),
) {
    val year = clock.today().year
    Column(modifier = modifier) {
        TitleText(freetraining.name)
        Text("Category: ${freetraining.category}")
        Text("Date: ${freetraining.date.prettyPrint(year)}")
        if (freetraining.wasCheckedin) {
            Text("${Icons.Lsc.checkedin} Was checked-in")
        }
    }
}
