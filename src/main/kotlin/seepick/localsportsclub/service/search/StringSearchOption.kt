package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging.logger

class StringSearchOption<T>(
    label: String,
    private val stringExtractors: List<(T) -> String>,
    private val resetPredicates: () -> Unit,
    private val resetItems: () -> Unit,
) : SearchOption<T>(label) {

    private val log = logger {}
    var searchTerm by mutableStateOf("")
        private set
    private val terms = mutableListOf<String>()

    fun setSearchInput(givenInput: String) {
        searchTerm = givenInput
        givenInput.trim().also {
            if (it.isEmpty()) {
                terms.clear()
                resetPredicates()
            } else {
                terms.clear()
                terms.addAll(it.split(" ").filter { it.isNotEmpty() }.distinct().map { it.lowercase() })
                log.debug { "Set search terms to: $terms" }
                resetPredicates()
            }
        }
        resetItems()
    }

    override fun buildPredicate(): (T) -> Boolean =
        if (terms.isEmpty()) alwaysTrue
        else { item ->
            terms.all { term ->
                stringExtractors.any { stringExtractor ->
                    stringExtractor(item).lowercase().contains(term)
                }
            }
        }
}
