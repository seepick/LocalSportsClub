package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
) : SearchOption<T>(label, reset, initiallyEnabled) {


    var allSelects: List<SearchSelect> = mutableListOf(*allOptions.map { SearchSelect(it) }.toTypedArray())
        private set

    fun toggleSelect(select: SearchSelect) {
        allSelects.single { it == select }.toggleSelected()
        reset()
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
