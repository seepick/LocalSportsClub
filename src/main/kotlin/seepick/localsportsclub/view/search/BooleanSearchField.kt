package seepick.localsportsclub.view.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import seepick.localsportsclub.LocalCheckboxColors
import seepick.localsportsclub.service.search.BooleanSearchOption
import seepick.localsportsclub.view.common.applyTestTag

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> BooleanSearchField(
    searchOption: BooleanSearchOption<T>,
    labelTestTag: String? = null,
    checkboxTestTag: String? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        searchOption.ClickableSearchText(testTag = labelTestTag)
        if (searchOption.enabled) {
            Box {
                // remove implicit padding
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    Checkbox(
                        checked = searchOption.searchBoolean,
                        enabled = searchOption.enabled,
                        onCheckedChange = { searchOption.updateSearchBoolean(it) },
                        modifier = Modifier.applyTestTag(checkboxTestTag),
                        colors = LocalCheckboxColors.current,
                    )
                }
            }
        }
    }
}
