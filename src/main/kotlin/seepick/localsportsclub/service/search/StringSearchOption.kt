package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.view.common.VisualIndicator

class StringSearchOption<T>(
    label: String,
    private val stringExtractors: List<(T) -> String?>,
    permanentEnabled: Boolean = false,
    reset: () -> Unit,
    visualIndicator: VisualIndicator = VisualIndicator.NoIndicator
) : SearchOption<T>(label, reset, permanentEnabled, visualIndicator) {

    private val log = logger {}
    var searchTerm by mutableStateOf("")
        private set
    private val terms = mutableListOf<String>()

    override val permanentEnabledIsModified get() = searchTerm != ""
    override fun resetPermanentEnabledState() {
        searchTerm = ""
        terms.clear()
    }

    fun setSearchInput(givenInput: String) {
        searchTerm = givenInput
        givenInput.trim().also { trimmedInput ->
            terms.clear()
            if (trimmedInput.isNotEmpty()) {
                terms.addAll(trimmedInput.split(" ").filter { it.isNotEmpty() }.distinct().map { it.lowercase() })
            }
            log.debug { "Set search terms to: $terms" }
        }
        reset()
    }

    override fun buildPredicate(): (T) -> Boolean =
        if (terms.isEmpty()) alwaysTruePredicate
        else { item ->
            terms.all { term ->
                stringExtractors.any { stringExtractor ->
                    stringExtractor(item)?.lowercase()?.contains(term) ?: false
                }
            }
        }
}
