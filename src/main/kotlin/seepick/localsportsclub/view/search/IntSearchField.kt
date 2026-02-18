package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.search.FullNumericComparator
import seepick.localsportsclub.service.search.IntSearchOption
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.PaddingMode
import seepick.localsportsclub.view.common.TextFieldSlim
import seepick.localsportsclub.view.common.WidthOrFill

@Composable
fun <T> IntSearchField(searchOption: IntSearchOption<T>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.ClickableSearchText()
        if (searchOption.enabled) {
            DropDownTextField(
                items = FullNumericComparator.entries,
                selectedItem = searchOption.searchComparator,
                onItemSelected = { searchOption.updateSearchComparator(it) },
                enabled = searchOption.enabled,
                textSize = WidthOrFill.Width(80.dp),
                useSlimDisplay = true,
            )
            Spacer(Modifier.width(5.dp))
            TextFieldSlim(
                value = searchOption.searchInt?.toString() ?: "",
                singleLine = true,
                modifier = Modifier.width(50.dp),
                enabled = searchOption.enabled,
                paddingMode = PaddingMode.Horizontal,
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
