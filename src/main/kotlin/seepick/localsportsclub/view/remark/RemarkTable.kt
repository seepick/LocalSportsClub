package seepick.localsportsclub.view.remark

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import seepick.localsportsclub.service.model.RemarkRating
import seepick.localsportsclub.view.LocalTextFieldColors
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.LscVScroll
import seepick.localsportsclub.view.common.ModifierWith
import seepick.localsportsclub.view.common.SuggestTextField
import seepick.localsportsclub.view.common.Tooltip
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.WidthOrFill
import seepick.localsportsclub.view.common.WidthOrWeight
import seepick.localsportsclub.view.common.scrollbarWidthPadding
import seepick.localsportsclub.view.common.table.CellRenderer
import seepick.localsportsclub.view.common.table.SortableColumn
import seepick.localsportsclub.view.common.table.TableColumn
import seepick.localsportsclub.view.common.table.renderComposable
import seepick.localsportsclub.view.common.table.renderTableHeader

@Composable
fun RemarkTable(
    nameSuggestions: List<String>,
    onDelete: (RemarkViewEntity) -> Unit,
    remarks: List<RemarkViewEntity>,
    boxModifier: Modifier = Modifier.Companion,
) {
    val remarkColumns = buildRemarkColumns(
        suggestions = nameSuggestions, //viewModel.nameSuggestions,
        onDelete = onDelete, //{ viewModel.deleteRemark(it) },
    )
    Box(modifier = Modifier.fillMaxWidth().then(boxModifier)) {
        val tableScrollState = rememberLazyListState()
        LazyColumn(
            state = tableScrollState,
            modifier = Modifier.padding(end = scrollbarWidthPadding),
        ) {
            renderTableHeader(
                columns = remarkColumns,
                solidBg = Lsc.colors.surface,
                rowHeight = 45.dp,
                fontSize = 15.sp,
            )
            itemsIndexed(remarks) { _, item ->
                Row(verticalAlignment = Alignment.Bottom) {
                    remarkColumns.forEach { remarkCol ->
                        renderComposable(item, remarkCol)
                    }
                }
            }
        }
        LscVScroll(rememberScrollbarAdapter(tableScrollState))
    }
}

fun <T> simpleTableColumn(
    headerTitle: String,
    width: Dp? = null,
    weight: Float? = null,
    overrideHeaderBg: Color? = null,
    renderer: @Composable RowScope.(T, TableColumn<T>) -> Unit,
) = TableColumn(
    header = VisualIndicator.StringIndicator(headerTitle),
    size = WidthOrWeight.ofEither(width, weight),
    renderer = CellRenderer.CustomRenderer(renderer),
    sorting = SortableColumn.Disabled,
    overrideHeaderBg = overrideHeaderBg,
)

fun buildRemarkColumns(
    suggestions: List<String>,
    onDelete: (RemarkViewEntity) -> Unit,
) = listOf<TableColumn<RemarkViewEntity>>(
    simpleTableColumn("Name", width = 300.dp) { remark, col ->
        Row(ModifierWith(col.size)) {
            SuggestTextField(
                text = remark.name,
                width = 300.dp,
                suggestions = suggestions,
//              textModifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier,
            )
        }
    },
    simpleTableColumn("Rating", width = 150.dp) { remark, col ->
        Row(ModifierWith(col.size)) {
            DropDownTextField(
                items = RemarkRating.entries,
                useSlimDisplay = true,
                slimHeight = 56.dp,
                itemFormatter = { "${it.emoji} ${it.label}" },
                selectedItem = remark.rating,
                onItemSelected = { remark.rating = it },
                textSize = WidthOrFill.FillWidth,
            )
        }
    },
    simpleTableColumn("Note", weight = 0.7f) { remark, col ->
        Row(ModifierWith(col.size)) {
            TextField(
                value = remark.remark,
                onValueChange = { remark.remark = it },
                colors = LocalTextFieldColors.current,
                modifier = Modifier.weight(1f),
            )
        }
    },
    simpleTableColumn("", width = 80.dp, overrideHeaderBg = Lsc.colors.customDialogSurface) { remark, col ->
        Row(ModifierWith(col.size)) {
            Tooltip("Delete") {
                TextButton(
                    onClick = { onDelete(remark) },
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    },
)
