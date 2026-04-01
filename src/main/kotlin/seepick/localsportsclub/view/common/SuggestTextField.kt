package seepick.localsportsclub.view.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.view.LocalTextFieldColors
import seepick.localsportsclub.view.Lsc

@Composable
fun SuggestTextField(
    text: MutableState<String>,
    suggestions: List<String>,
    width: Dp = 200.dp,
    textModifier: Modifier = Modifier.Companion,
) {
    val isMenuExpanded = remember { mutableStateOf(false) }
    val icon = if (isMenuExpanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    Column {
        TextField(
            value = text.value,
            singleLine = true,
            onValueChange = { text.value = it },
            colors = LocalTextFieldColors.current,
            trailingIcon = {
                if (suggestions.isEmpty()) {
                    null
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Lsc.colors.clickableNeutral,
                        modifier = Modifier.clickable { isMenuExpanded.value = !isMenuExpanded.value },
                    )
                }
            },
            modifier = Modifier.width(width).then(textModifier),
        )
        if (suggestions.isNotEmpty()) {
            DropdownMenuX(
                items = suggestions,
                itemFormatter = { it },
                isMenuExpanded = isMenuExpanded,
                width = width,
                onItemClicked = { text.value = it },
                selectedItem = text.value, // try it, if possible ;)
            )
        }
    }
}
