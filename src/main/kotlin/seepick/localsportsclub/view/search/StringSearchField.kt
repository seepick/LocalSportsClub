package seepick.localsportsclub.view.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.service.search.StringSearchOption

@Composable
fun <T> StringSearchField(searchOption: StringSearchOption<T>) {
    OutlinedTextField(
        value = searchOption.searchTerm,
        label = {
            Text(
                text = searchOption.label,
                style = TextStyle(fontSize = 12.sp),
            )
        },
        singleLine = true,
        leadingIcon = {
            if (searchOption.searchTerm.isNotEmpty()) {
                Icon(Icons.Default.Close, null, Modifier.let {
                    if (searchOption.enabled) {
                        it.clickable {
                            searchOption.setSearchInput("")
                        }
                    } else it
                })
            }
        },
        modifier = Modifier
            .width(200.dp)
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Escape && e.type == KeyEventType.KeyUp) {
                    searchOption.setSearchInput("")
                }
                false
            },
        onValueChange = {
            searchOption.setSearchInput(it)
        },
    )
}
