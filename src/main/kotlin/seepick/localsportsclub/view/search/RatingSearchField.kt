package seepick.localsportsclub.view.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.service.search.IntSearchComparator
import seepick.localsportsclub.service.search.RatingSearchOption

@Composable
fun <T> RatingSearchField(searchOption: RatingSearchOption<T>) {
    var isComparatorMenuExpanded by remember { mutableStateOf(false) }
    var isRatingMenuExpanded by remember { mutableStateOf(false) }
    var comparatorTextFieldSize by remember { mutableStateOf(Size.Zero) }
    var ratingTextFieldSize by remember { mutableStateOf(Size.Zero) }
    val comparatorIcon = if (isComparatorMenuExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
    val ratingIcon = if (isRatingMenuExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown

    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.buildClickableText()
        if (searchOption.enabled) {
            if (searchOption.enabled) {
                Column {
                    OutlinedTextField(
                        value = searchOption.searchComparator.symbol,
                        onValueChange = { /* no-op */ },
                        readOnly = true,
                        enabled = searchOption.enabled,
                        modifier = Modifier.width(100.dp).onGloballyPositioned { coordinates ->
                            comparatorTextFieldSize = coordinates.size.toSize()
                        }.onFocusChanged { state ->
                            isComparatorMenuExpanded = state.isFocused
                        },
                        trailingIcon = {
                            Icon(comparatorIcon, null, Modifier.let {
                                if (searchOption.enabled) {
                                    it.clickable {
                                        isComparatorMenuExpanded = !isComparatorMenuExpanded
                                    }
                                } else it
                            })
                        },
                    )
                    DropdownMenu(
                        expanded = isComparatorMenuExpanded,
                        onDismissRequest = { isComparatorMenuExpanded = false },
                        modifier = Modifier.width(with(LocalDensity.current) { comparatorTextFieldSize.width.toDp() })
                    ) {
                        IntSearchComparator.entries.forEach { comparator ->
                            DropdownMenuItem(onClick = {
                                searchOption.updateSearchComparator(comparator)
                                isComparatorMenuExpanded = false
                            }) {
                                Text(text = comparator.symbol)
                            }
                        }
                    }
                }
                Column {

                    OutlinedTextField(
                        value = searchOption.searchRating.string,
                        onValueChange = { /* no-op */ },
                        singleLine = true,
                        readOnly = true,
                        enabled = searchOption.enabled,
                        modifier = Modifier.width(160.dp).onGloballyPositioned { coordinates ->
                            ratingTextFieldSize = coordinates.size.toSize()
                        }.onFocusChanged { state ->
                            isRatingMenuExpanded = state.isFocused
                        },
                        trailingIcon = {
                            Icon(ratingIcon, null, Modifier.let {
                                if (searchOption.enabled) {
                                    it.clickable {
                                        isRatingMenuExpanded = !isRatingMenuExpanded
                                    }
                                } else it
                            })
                        },
                    )
                    DropdownMenu(
                        expanded = isRatingMenuExpanded,
                        onDismissRequest = { isRatingMenuExpanded = false },
                        modifier = Modifier.width(with(LocalDensity.current) { ratingTextFieldSize.width.toDp() })
                    ) {
                        Rating.entries.forEach { rating ->
                            DropdownMenuItem(onClick = {
                                searchOption.updateSearchRating(rating)
                                isRatingMenuExpanded = false
                            }) {
                                Text(text = rating.string)
                            }
                        }
                    }
                }
            }
        }
    }
}
