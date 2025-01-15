package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import seepick.localsportsclub.service.search.BooleanSearchOption

@Composable
fun <T> BooleanSearchField(searchOption: BooleanSearchOption<T>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(searchOption.label)
        Switch(checked = searchOption.enabled, onCheckedChange = { searchOption.updateEnabled(it) })
        if (searchOption.enabled) {
            Checkbox(
                checked = searchOption.searchBoolean,
                enabled = searchOption.enabled,
                onCheckedChange = { searchOption.updateSearchBoolean(it) },
            )
        }
    }
}
