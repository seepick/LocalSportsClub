package seepick.localsportsclub.view.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.table.rowBgColor

@Composable
fun <T : HasLabel> DropdownMenuX(
    items: List<T>,
    isMenuExpanded: MutableState<Boolean>,
    width: Dp,
    onItemClicked: (T) -> Unit,
    selectedItem: T?,
    itemAlign: TextAlign? = null,
) {
    _DropdownMenuX(
        items = items,
        isMenuExpanded = isMenuExpanded,
        width = width,
        onItemClicked = onItemClicked,
        selectedItem = selectedItem,
        itemAlign = itemAlign,
    )
}

@Composable
fun <T> DropdownMenuX(
    items: List<T>,
    itemFormatter: (T) -> String,
    isMenuExpanded: MutableState<Boolean>,
    width: Dp,
    onItemClicked: (T) -> Unit,
    selectedItem: T?,
    itemAlign: TextAlign? = null,
) {
    _DropdownMenuX(
        items = items,
        isMenuExpanded = isMenuExpanded,
        itemFormatter = itemFormatter,
        width = width,
        onItemClicked = onItemClicked,
        selectedItem = selectedItem,
        itemAlign = itemAlign,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T> _DropdownMenuX(
    items: List<T>,
    itemFormatter: ((T) -> String)? = null,
    isMenuExpanded: MutableState<Boolean>,
    width: Dp,
    onItemClicked: (T) -> Unit,
    selectedItem: T?,
    itemAlign: TextAlign? = null,
) {
    DropdownMenu(
        expanded = isMenuExpanded.value,
        onDismissRequest = { isMenuExpanded.value = false },
        modifier = Modifier.width(width)
    ) {
        var index = 0
        items.forEach { item ->
            var isHovered by remember { mutableStateOf(false) }
            index++
            DropdownMenuItem(
                onClick = {
                    onItemClicked(item)
                    isMenuExpanded.value = false
                },
                modifier = Modifier
                    .height(35.dp)
                    .onPointerEvent(PointerEventType.Enter) { isHovered = true }
                    .onPointerEvent(PointerEventType.Exit) { isHovered = false }
                    .background(
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
                    fontSize = 13.sp,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = itemAlign,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
