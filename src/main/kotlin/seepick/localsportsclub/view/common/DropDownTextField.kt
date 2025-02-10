@file:Suppress("UNCHECKED_CAST")

package seepick.localsportsclub.view.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
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
    enabled: Boolean = true,
    textSize: WidthOrFill,
    textFieldEdits: DropDownTextFieldEdits? = null,
    useSlimDisplay: Boolean = false,
) {
    _DropDownTextField(
        label = label,
        items = items,
        selectedItem = selectedItem,
        onItemSelected = onItemSelected,
        enabled = enabled,
        textSize = textSize,
        textFieldEdits = textFieldEdits,
        useSlimDisplay = useSlimDisplay,
    )
}

@Composable
fun <T> DropDownTextField(
    items: List<T>,
    itemFormatter: (T) -> String,
    onItemSelected: (T) -> Unit,
    label: String? = null,
    selectedItem: T,
    enabled: Boolean = true,
    textSize: WidthOrFill,
    textFieldEdits: DropDownTextFieldEdits? = null,
    useSlimDisplay: Boolean = false,
) {
    _DropDownTextField(
        label = label,
        items = items,
        itemFormatter = itemFormatter,
        selectedItem = selectedItem,
        onItemSelected = onItemSelected,
        enabled = enabled,
        textSize = textSize,
        textFieldEdits = textFieldEdits,
        useSlimDisplay = useSlimDisplay,
    )
}

@Composable
private fun <T> _DropDownTextField(
    label: String? = null,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemFormatter: ((T) -> String)? = null,
    enabled: Boolean,
    textSize: WidthOrFill,
    textFieldEdits: DropDownTextFieldEdits? = null,
    useSlimDisplay: Boolean
) {
    val selectedItemLabel = if (selectedItem == null) {
        label ?: ""
    } else {
        (selectedItem as? HasLabel)?.label ?: itemFormatter!!(selectedItem)
    }
    val source = remember {
        MutableInteractionSource()
    }
    val isMenuExpanded = remember { mutableStateOf(false) }
    if (source.collectIsPressedAsState().value) {
        // modifier.onClick/clickable not working...
        isMenuExpanded.value = !isMenuExpanded.value
    }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val icon = if (isMenuExpanded.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Column {
        if (textFieldEdits == null) {
            if (useSlimDisplay) {
                TextFieldSlim(
                    value = selectedItemLabel,
                    onValueChange = { /* no-op */ },
                    readOnly = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    enabled = enabled,
                    singleLine = true,
                    nullifyContentPadding = true,
                    interactionSource = source,
                    modifier = Modifier
                        .widthOrFill(textSize)
//                        .padding(5.dp)
                        .defaultMinSize(minHeight = 1.dp).height(26.dp)
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        }
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            isFocused = state.isFocused
                        },
                    label = label?.let { { Text(label) } },
                    trailingIcon = {
                        Icon(icon,
                            null,
                            Modifier.focusRequester(focusRequester) // nice hack to remove focus from textfield ;)
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
                OutlinedTextField(
                    value = selectedItemLabel,
                    onValueChange = { /* no-op */ },
                    readOnly = true,
                    enabled = enabled,
                    singleLine = true,
                    modifier = Modifier
                        .widthOrFill(textSize)
                        .onGloballyPositioned { coordinates ->
                            textFieldSize = coordinates.size.toSize()
                        }
                        .onFocusChanged { state ->
                            isMenuExpanded.value = state.isFocused
                        },
                    label = label?.let { { Text(label) } },
                    trailingIcon = {
                        Icon(icon,
                            null,
                            Modifier.focusRequester(focusRequester) // nice hack to remove focus from textfield ;)
                                .let {
                                    if (enabled) {
                                        it.clickable {
                                            isMenuExpanded.value = !isMenuExpanded.value
                                        }
                                    } else it
                                })
                    },
                )
            }
        } else {
            TextFieldSlim(
                value = textFieldEdits.text.value,
                onValueChange = textFieldEdits.onTextChanged,
                readOnly = false,
                isError = textFieldEdits.errorChecker(),
                enabled = enabled,
                singleLine = true,
                nullifyContentPadding = useSlimDisplay,
                modifier = Modifier
                    .widthOrFill(textSize)
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
                    }
                    .let {
                        if (useSlimDisplay) {
                            it
                                .padding(0.dp)
                                .defaultMinSize(minHeight = 1.dp).height(26.dp)
                        } else it
                    },
                textStyle = textFieldEdits.textAlign?.let { LocalTextStyle.current.copy(textAlign = textFieldEdits.textAlign) }
                    ?: LocalTextStyle.current,
                label = label?.let { { Text(label) } },
                leadingIcon = {
                    if (textFieldEdits.onReset != null && selectedItem != null && enabled) {
                        Icon(Icons.Default.Close, null, Modifier.clickable { textFieldEdits.onReset.invoke() })
                    } else {
                        Spacer(Modifier.width(Icons.Default.Close.defaultWidth))
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
