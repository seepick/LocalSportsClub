package seepick.localsportsclub.view.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import seepick.localsportsclub.service.search.IntSearchComparator
import seepick.localsportsclub.service.search.IntSearchOption
import seepick.localsportsclub.view.common.LscDropdownMenu

@Composable
fun <T> IntSearchField(searchOption: IntSearchOption<T>) {
    var isMenuExpanded = remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val icon = if (isMenuExpanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.buildClickableText()
        if (searchOption.enabled) {
            Column {
                OutlinedTextField(
                    value = searchOption.searchComparator.symbol,
                    onValueChange = { /* no-op */ },
                    readOnly = true,
                    enabled = searchOption.enabled,
                    modifier = Modifier.width(100.dp).onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    }.onFocusChanged { state ->
                        isMenuExpanded.value = state.isFocused
                    },
                    trailingIcon = {
                        Icon(icon, null, Modifier.let {
                            if (searchOption.enabled) {
                                it.clickable {
                                    isMenuExpanded.value = !isMenuExpanded.value
                                }
                            } else it
                        })
                    },
                )
                LscDropdownMenu(
                    items = IntSearchComparator.entries,
                    isMenuExpanded = isMenuExpanded,
                    textFieldSize = textFieldSize,
                    onItemClicked = { searchOption.updateSearchComparator(it) },
                    selectedItem = searchOption.searchComparator,
                )
            }
            OutlinedTextField(
                value = searchOption.searchInt?.toString() ?: "",
                singleLine = true,
                modifier = Modifier.width(60.dp),
                enabled = searchOption.enabled,
                onValueChange = {
                    if (it.isEmpty()) {
                        searchOption.updateSearchInt(null)
                    } else {
                        it.toIntOrNull()?.also {
                            searchOption.updateSearchInt(it)
                        }
                    }
                },
            )
        }
    }
}
