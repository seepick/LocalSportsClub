package seepick.localsportsclub.view.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.service.search.SelectSearchOption
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.LscVScroll
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.table.VDirection
import seepick.localsportsclub.view.common.table.rowBgColor

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun <T> SelectSearchField(
    searchOption: SelectSearchOption<T>,
    width: Dp = 150.dp,
) {
    val focusRequester = remember { FocusRequester() }
    val tableScrollState = rememberLazyListState()
    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.ClickableSearchText()
        if (searchOption.enabled) {
            Tooltip("Press meta-key and click to expand selection") {
                Box(
                    modifier = Modifier.height(60.dp).width(width).border(1.dp, color = Lsc.colors.clickableNeutral)
                        .focusProperties { canFocus = true }.focusRequester(focusRequester).onKeyEvent {
                            if (it.type == KeyEventType.KeyDown) {
                                when (it.key) {
                                    Key.DirectionUp -> VDirection.Up
                                    Key.DirectionDown -> VDirection.Down
                                    else -> null
                                }?.also { searchOption.onItemNavigation(it) }
                            }
                            false
                        }) {
                    LazyColumn(
                        state = tableScrollState,
                        modifier = Modifier.padding(end = 12.dp) // for the scrollbar to the right
                    ) {
                        itemsIndexed(searchOption.allSelects) { index, select ->
                            var isHovered by remember { mutableStateOf(false) }
                            val backgroundColor = if (searchOption.enabled) rowBgColor(
                                index = index,
                                isHovered = isHovered,
                                isSelected = select.isSelected,
                                isClickable = true,
                            ) else MaterialTheme.colors.background
                            Text(
                                text = select.opt.renderedLabel,
                                color = Lsc.colors.onBackground,
                                fontSize = 10.sp,
                                modifier = Modifier.fillMaxWidth(1.0f)
                                    .background(backgroundColor) // must be before padding!
                                    .padding(start = 4.dp).onPointerEvent(PointerEventType.Enter) { isHovered = true }
                                    .onPointerEvent(PointerEventType.Exit) { isHovered = false }.let { m ->
                                        if (searchOption.enabled) {
                                            m.onClick {
                                                focusRequester.requestFocus()
                                                searchOption.toggleSelect(select)
                                            }
                                        } else m
                                    })
                        }
                    }
                    LscVScroll(rememberScrollbarAdapter(tableScrollState))
                }
            }
        }
    }
}
