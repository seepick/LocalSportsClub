@file:Suppress("UNCHECKED_CAST")

package seepick.localsportsclub.view.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize

data class DropDownTextFieldEdits(
    val text: MutableState<String>,
    val onTextChanged: (String) -> Unit,
    val errorChecker: () -> Boolean = { false },
    val textAlign: TextAlign? = null,
    val onReset: (() -> Unit)? = null,
)

@Composable
fun <T : HasLabel> DropDownTextField(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    label: String? = null,
    enabled: Boolean,
    textWidth: Dp = 200.dp,
    textFieldEdits: DropDownTextFieldEdits? = null,
) {
    _DropDownTextField(
        label = label,
        items = items,
        selectedItem = selectedItem,
        onItemSelected = onItemSelected,
        enabled = enabled,
        textWidth = textWidth,
        textFieldEdits = textFieldEdits,
    )
}

@Composable
fun <T> DropDownTextField(
    items: List<T>,
    itemFormatter: (T) -> String,
    onItemSelected: (T) -> Unit,
    label: String? = null,
    selectedItem: T,
    enabled: Boolean,
    textWidth: Dp = 200.dp,
    textFieldEdits: DropDownTextFieldEdits? = null,
) {
    _DropDownTextField(
        label = label,
        items = items,
        itemFormatter = itemFormatter,
        selectedItem = selectedItem,
        onItemSelected = onItemSelected,
        enabled = enabled,
        textWidth = textWidth,
        textFieldEdits = textFieldEdits,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> _DropDownTextField(
    label: String? = null,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemFormatter: ((T) -> String)? = null,
    enabled: Boolean,
    textWidth: Dp = 200.dp,
    textFieldEdits: DropDownTextFieldEdits? = null,
) {
    val selectedItemLabel = (selectedItem as? HasLabel)?.label ?: itemFormatter!!(selectedItem)
    val isMenuExpanded = remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val icon = if (isMenuExpanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    val focusRequester = remember { FocusRequester() }

    Column {
        if (textFieldEdits == null) {
            OutlinedTextField(
                value = selectedItemLabel,
                onValueChange = { /* no-op */ },
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.width(textWidth)
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    }
                    .onFocusChanged { state ->
                        isMenuExpanded.value = state.isFocused
                    },
                label = label?.let { { Text(label) } },
                trailingIcon = {
                    Icon(icon, null, Modifier
                        .focusRequester(focusRequester) // nice hack to remove focus from textfield ;)
                        .let {
                            if (enabled) {
                                it.clickable {
                                    isMenuExpanded.value = !isMenuExpanded.value
                                }
                            } else it
                        })
                },
            )
        } else {
            TextField(
                value = textFieldEdits.text.value,
                onValueChange = textFieldEdits.onTextChanged,
                readOnly = false,
                isError = textFieldEdits.errorChecker(),
                enabled = enabled,
                singleLine = true,
                modifier = Modifier.width(textWidth)
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    }
                    .let { m ->
                        textFieldEdits.onReset?.let { reset ->
                            m.onPreviewKeyEvent { e ->
                                if (e.key == Key.Escape && e.type == KeyEventType.KeyUp) {
                                    reset()
                                }
                                false
                            }
                        } ?: m
                    },
                textStyle = textFieldEdits.textAlign?.let { LocalTextStyle.current.copy(textAlign = textFieldEdits.textAlign) }
                    ?: LocalTextStyle.current,
                label = label?.let { { Text(label) } },
                leadingIcon = textFieldEdits.onReset?.let { reset ->
                    {
                        Icon(Icons.Default.Close, null, Modifier.let {
                            if (enabled) {
                                it.clickable {
                                    reset()
                                }
                            } else it
                        })
                    }
                },
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
        }
        val onClickFocus = {
            if (textFieldEdits == null) {
                focusRequester.requestFocus()
            }
        }
        if (itemFormatter != null) {
            DropdownMenuX(
                items = items,
                isMenuExpanded = isMenuExpanded,
                itemFormatter = itemFormatter,
                textFieldSize = textFieldSize,
                onItemClicked = {
                    onItemSelected(it)
                    onClickFocus()
                },
                selectedItem = selectedItem,
            )
        } else {
            DropdownMenuX(
                items = items as List<HasLabel>,
                isMenuExpanded = isMenuExpanded,
                textFieldSize = textFieldSize,
                onItemClicked = {
                    onItemSelected(it as T)
                    onClickFocus()
                },
                selectedItem = selectedItem as HasLabel,
            )
        }
    }
}
