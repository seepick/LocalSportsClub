package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.GlobalKeyboard
import seepick.localsportsclub.view.common.VisualIndicator
import seepick.localsportsclub.view.common.table.VDirection
import seepick.localsportsclub.view.common.table.navigate

data class SearchSelect(
    val text: String,
) {
    var isSelected: Boolean by mutableStateOf(false)
    fun toggleSelected() {
        isSelected = !isSelected
    }
}

class SelectSearchOption<T>(
    label: String,
    allOptions: List<String>,
    private val extractor: (T) -> List<String>,
    initiallyEnabled: Boolean = false,
    reset: () -> Unit,
    visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
    private val globalKeyboard: GlobalKeyboard,
) : SearchOption<T>(label, reset, initiallyEnabled, visualIndicator) {

    var allSelects: List<SearchSelect> = mutableListOf(*allOptions.map { SearchSelect(it) }.toTypedArray())
        private set

    private var recentSearchSelect: SearchSelect? = null

    fun toggleSelect(select: SearchSelect) {
        if (!select.isSelected && !globalKeyboard.isMetaKeyDown) {
            disableAllSelected()
        }
        if (!select.isSelected) {
            recentSearchSelect = select
        }
        select.toggleSelected()
        reset()
    }

    fun onItemNavigation(direction: VDirection) {
        recentSearchSelect?.also { selected ->
            val target = allSelects.navigate(selected, direction)
            if (target != null) {
                recentSearchSelect = target
                disableAllSelected()
                target.toggleSelected()
                reset()
            }
        }
    }

    private fun disableAllSelected() {
        allSelects.filter { it.isSelected }.forEach { it.toggleSelected() }
    }

    override fun buildPredicate(): (T) -> Boolean {
        val selected = allSelects.filter { it.isSelected }.map { it.text }
        return if (selected.isEmpty()) alwaysTruePredicate
        else { item ->
            val itemsOptions = extractor(item)
            selected.any { itemsOptions.contains(it) }
        }
    }
}
