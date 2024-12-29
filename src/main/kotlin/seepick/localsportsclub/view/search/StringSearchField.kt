package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
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
