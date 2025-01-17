package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.search.IntSearchComparator
import seepick.localsportsclub.service.search.IntSearchOption
import seepick.localsportsclub.view.common.DropDownTextField

@Composable
fun <T> IntSearchField(searchOption: IntSearchOption<T>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.ClickableSearchText()
        if (searchOption.enabled) {
            DropDownTextField(
                items = IntSearchComparator.entries,
                selectedItem = searchOption.searchComparator,
                onItemSelected = { searchOption.updateSearchComparator(it) },
                enabled = searchOption.enabled,
                textWidth = 100.dp,
            )
            OutlinedTextField(
                value = searchOption.searchInt?.toString() ?: "",
                singleLine = true,
                modifier = Modifier.width(60.dp),
                enabled = searchOption.enabled,
                onValueChange = { intString ->
                    if (intString.isEmpty()) {
                        searchOption.updateSearchInt(null)
                    } else {
                        intString.toIntOrNull()?.also {
                            searchOption.updateSearchInt(it)
                        }
                    }
                },
            )
        }
    }
}
