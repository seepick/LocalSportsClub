package seepick.localsportsclub.view.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.view.LocalTextFieldColors

@Composable
fun SuggestTextField(
    text: MutableState<String>,
    suggestions: List<String>,
    width: Dp = 200.dp,
    textModifier: Modifier = Modifier.Companion,
) {
    val dropdownVisible = remember { mutableStateOf(false) }
    Column {
        TextField(
            value = text.value,
            singleLine = true,
            onValueChange = { text.value = it },
            colors = LocalTextFieldColors.current,
            modifier = Modifier.width(width).onFocusChanged { focusState ->
                dropdownVisible.value = focusState.isFocused
            }.then(textModifier),
        )
        if (suggestions.isNotEmpty()) {
            DropdownMenuX(
                items = suggestions,
                itemFormatter = { it },
                isMenuExpanded = dropdownVisible,
                width = width,
                onItemClicked = { text.value = it },
                selectedItem = text.value, // try it, if possible ;)
            )
        }
    }
}
