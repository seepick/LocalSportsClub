package seepick.localsportsclub.view.venue.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import seepick.localsportsclub.service.model.Freetraining
import seepick.localsportsclub.service.prettyPrint
import seepick.localsportsclub.service.prettyPrintWith
import seepick.localsportsclub.view.LscIcons

@Composable
fun SimpleFreetrainingsTable(freetrainings: SnapshotStateList<Freetraining>?, currentYear: Int) {
    if (freetrainings?.isNotEmpty() == true) {
        Text("Freetrainings:")
        LazyColumn {
            items(freetrainings) { freetraining ->
                Row {
                    if (freetraining.checkedinTime != null) {
                        Text(text = LscIcons.checkedin)
                    }
                    Text("${freetraining.name} / ${freetraining.category}: ")
                    Text(
                        text = if (freetraining.checkedinTime == null) {
                            freetraining.date.prettyPrint(currentYear)
                        } else {
                            freetraining.date.prettyPrintWith(freetraining.checkedinTime!!, currentYear)
                        }
                    )
                }
            }
        }
    }
}
