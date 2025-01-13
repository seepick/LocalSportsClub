package seepick.localsportsclub.view.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.service.search.SelectSearchOption

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> SelectSearchField(searchOption: SelectSearchOption<T>) {
    val tableScrollState = rememberLazyListState()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(searchOption.label)
        Checkbox(checked = searchOption.enabled, onCheckedChange = { searchOption.updateEnabled(it) })
        Box(
            modifier = Modifier
                .height(60.dp)
                .width(200.dp)
                .border(1.dp, Color.Black)
        ) {
            LazyColumn(
                state = tableScrollState, modifier = Modifier.padding(end = 12.dp) // for the scrollbar to the right
            ) {
                itemsIndexed(searchOption.allSelects) { index, select ->
                    val backgroundColor = if (select.selected) Color.Green
                    else if (index % 2 == 0) Color.LightGray
                    else null
                    Text(
                        text = select.text,
                        modifier = Modifier
                            .fillMaxWidth(1.0f)
                            .let { m ->
                                if (searchOption.enabled) {
                                    m.onClick {
                                        searchOption.toggleSelect(select)
                                    }
                                } else m
                            }.let { m ->
                                backgroundColor?.let { color ->
                                    m.background(color)
                                } ?: m
                            }
                    )
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(), adapter = rememberScrollbarAdapter(
                    scrollState = tableScrollState
                )
            )
        }
    }
}
