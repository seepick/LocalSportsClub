package seepick.localsportsclub.view.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import seepick.localsportsclub.Lsc

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> LscDropdownMenu(
    items: List<T>,
    isMenuExpanded: MutableState<Boolean>,
    textFieldSize: Size,
    onItemClicked: (T) -> Unit,
    selectedItem: T,
    itemFormatter: ((T) -> String)? = null,
) {
    DropdownMenu(
        expanded = isMenuExpanded.value,
        onDismissRequest = { isMenuExpanded.value = false },
        modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
    ) {
        var index = 0
        items.forEach { item ->
            var isHovered by remember { mutableStateOf(false) }
            index++
            DropdownMenuItem(onClick = {
                onItemClicked(item)
                isMenuExpanded.value = false
            },
                modifier = Modifier.onPointerEvent(PointerEventType.Enter) { isHovered = true }
                    .onPointerEvent(PointerEventType.Exit) { isHovered = false }.background(
                        rowBgColor(
                            index = index,
                            isHovered = isHovered,
                            isSelected = item == selectedItem,
                            isClickable = true,
                        )
                    )
            ) {
                Text(
                    color = Lsc.colors.onBackground,
                    text = if (item is HasLabel) item.label else itemFormatter!!(item),
                )
            }
        }
    }
}
