package seepick.localsportsclub.view.remark

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import seepick.localsportsclub.LocalTextFieldColors
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.model.RemarkRating
import seepick.localsportsclub.view.common.DropDownTextField
import seepick.localsportsclub.view.common.DropdownMenuX
import seepick.localsportsclub.view.common.LscVScroll
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
fun SuggestTextField(
    text: MutableState<String>,
    suggestions: List<String>,
    width: Dp = 200.dp,
    textModifier: Modifier = Modifier,
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

fun <T> simpleTableColumn(
    headerTitle: String,
    width: Dp? = null,
    weight: Float? = null,
    renderer: @Composable RowScope.(T, TableColumn<T>) -> Unit,
) = TableColumn(
    header = VisualIndicator.StringIndicator(headerTitle),
    size = WidthOrWeight.ofEither(width, weight),
    renderer = CellRenderer.CustomRenderer(renderer),
    sorting = SortableColumn.Disabled,
)

fun buildRemarkColumns(
    suggestions: List<String>,
    onDelete: (RemarkViewEntity) -> Unit,
) =
    listOf<TableColumn<RemarkViewEntity>>(
        simpleTableColumn("Name", weight = 0.3f) { remark, _ ->
            SuggestTextField(
                text = remark.name,
                width = 300.dp,
                suggestions = suggestions,
//                    TODO textModifier = if (index == 0) Modifier.focusRequester(focusRequester) else Modifier,
            )
        },
        simpleTableColumn("Rating", width = 300.dp) { remark, _ ->
            DropDownTextField(
                items = RemarkRating.entries,
                useSlimDisplay = true,
                slimHeight = 56.dp,
                itemFormatter = { "${it.emoji} ${it.label}" },
                selectedItem = remark.rating,
                onItemSelected = { remark.rating = it },
                textSize = WidthOrFill.Width(150.dp)
            )
        },
        simpleTableColumn("Note", weight = 0.7f) { remark, _ ->
            TextField(
                value = remark.remark,
                onValueChange = { remark.remark = it },
                colors = LocalTextFieldColors.current,
                modifier = Modifier.weight(1f),
            )
        },
        simpleTableColumn("", width = 80.dp) { remark, _ ->
            Tooltip("Delete") {
                TextButton(
                    onClick = { onDelete(remark) },
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    )

@Composable
fun RemarkView(
    viewModel: RemarkViewModel = koinViewModel(),
) {
//    val focusRequester = remember { FocusRequester() }
//    var shouldRequestFocus by remember { mutableStateOf(false) }

//    LaunchedEffect(shouldRequestFocus) {
//        if (shouldRequestFocus && viewModel.remarks.isNotEmpty()) {
//            focusRequester.requestFocus()
//            shouldRequestFocus = false
//        }
//    }
    Column {
        Button(onClick = {
            viewModel.addNewRemark()
//            shouldRequestFocus = true
        }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add New")
        }
        Spacer(Modifier.height(5.dp))

        RemarkTable()
    }
}

@Composable
fun RemarkTable(
    viewModel: RemarkViewModel = koinViewModel(),
) {
    val remarkColumns = buildRemarkColumns(
        suggestions = viewModel.nameSuggestions,
        onDelete = { viewModel.deleteRemark(it) },
    )
    Box(
        modifier = Modifier
            .height(500.dp) // FIXME height layout hack
            .fillMaxWidth(),
    ) {
        val tableScrollState = rememberLazyListState()
        LazyColumn(
            state = tableScrollState,
            modifier = Modifier.padding(end = scrollbarWidthPadding),
        ) {
            renderTableHeader(remarkColumns, solidBg = Lsc.colors.onSurface)
            itemsIndexed(viewModel.remarks) { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    remarkColumns.forEach { remarkCol ->
                        renderComposable(item, remarkCol)
                    }
                }
            }
        }
        LscVScroll(rememberScrollbarAdapter(tableScrollState))
    }
}
