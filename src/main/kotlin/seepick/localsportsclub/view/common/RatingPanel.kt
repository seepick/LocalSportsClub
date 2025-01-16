package seepick.localsportsclub.view.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import seepick.localsportsclub.service.model.Rating

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RatingPanel(
    selectedRating: MutableState<Rating>,
    enabled: Boolean = true,
) {
    Column {
        var isMenuExpanded = remember { mutableStateOf(false) }
        var textFieldSize by remember { mutableStateOf(Size.Zero) }
        val icon = if (isMenuExpanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

        OutlinedTextField(
            value = selectedRating.value.label,
            onValueChange = { /* no-op */ },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier.width(250.dp).onGloballyPositioned { coordinates ->
                textFieldSize = coordinates.size.toSize()
            }.onFocusChanged { state ->
                isMenuExpanded.value = state.isFocused
            },
            label = { Text("Rating") },
            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
            trailingIcon = {
                Icon(icon, null, Modifier.let {
                    if (enabled) {
                        it.clickable {
                            isMenuExpanded.value = !isMenuExpanded.value
                        }
                    } else it
                })
            },
        )

        LscDropdownMenu(
            items = Rating.entries,
            isMenuExpanded = isMenuExpanded,
            textFieldSize = textFieldSize,
            onItemClicked = { selectedRating.value = it },
            selectedItem = selectedRating.value,
        )
    }
}
