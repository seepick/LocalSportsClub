package seepick.localsportsclub.service.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.seepick.uscclient.utils.DateTimeRange
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.view.common.VisualIndicator
import java.time.LocalDate

abstract class AbstractSearch<T>(
    private val resetItems: () -> Unit,
) {
    private val log = logger {}
    private val options = mutableListOf<SearchOption<T>>()
    private val predicates = mutableListOf<(T) -> Boolean>()
    val anyEnabled
        get() =
            options.any {
                !it.permanentEnabled && it.enabled ||
                        it.permanentEnabled && it.permanentEnabledIsModified
            }

    fun clearAll() {
        log.debug { "clearAll()" }
        options.filter { it.permanentEnabled }.forEach {
            it.resetPermanentEnabledState()
        }
        options.filter { it.enabled && !it.permanentEnabled }.forEach {
            it.updateEnabled(isEnabled = false, suppressReset = true)
        }
        reset()
    }

    protected fun newStringSearchOption(
        label: String,
        initiallyEnabled: Boolean = false,
        visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
        extractors: List<(T) -> String?>,
    ) =
        StringSearchOption(
            label = label,
            reset = ::reset,
            stringExtractors = extractors.toList(),
            permanentEnabled = initiallyEnabled,
            visualIndicator = visualIndicator,
        ).also {
            options += it
        }

    protected fun newDateTimeRangeSearchOption(
        label: String,
        initiallyEnabled: Boolean = false,
        visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
        extractor: (T) -> DateTimeRange,
    ) =
        DateTimeRangeSearchOption(
            label = label,
            reset = ::reset,
            extractor = extractor,
            initiallyEnabled = initiallyEnabled,
            visualIndicator = visualIndicator,
        ).also {
            options += it
        }

    protected fun newDateSearchOption(
        label: String,
        initialDate: LocalDate,
        initiallyEnabled: Boolean = false,
        visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
        extractor: (T) -> LocalDate,
    ) =
        DateSearchOption(
            label = label,
            reset = ::reset,
            dateExtractor = extractor,
            initiallyEnabled = initiallyEnabled,
            initialDate = initialDate,
            visualIndicator = visualIndicator,

            ).also {
            options += it
        }

    protected fun newBooleanSearchOption(
        label: String,
        initialValue: Boolean = false,
        initiallyEnabled: Boolean = false,
        visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
        extractor: (T) -> Boolean,
    ) =
        BooleanSearchOption(
            label = label,
            reset = ::reset,
            extractor = extractor,
            initialValue = initialValue,
            initiallyEnabled = initiallyEnabled,
            visualIndicator = visualIndicator,
        ).also {
            options += it
        }

    protected fun newIntSearchOption(
        label: String,
        initialValue: Int? = null,
        initiallyEnabled: Boolean = false,
        visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
        extractor: (T) -> Int,
    ) =
        IntSearchOption(
            label = label,
            reset = ::reset,
            extractor = extractor,
            initialValue = initialValue,
            initiallyEnabled = initiallyEnabled,
            visualIndicator = visualIndicator,
        ).also {
            options += it
        }

    fun <C : NumericComparator> newDoubleSearchOption(
        initialComparator: C,
        label: String,
        initialValue: Double? = null,
        initiallyEnabled: Boolean = false,
        visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
        extractor: (T) -> Double,
    ) =
        DoubleSearchOption<T, C>(
            initialComparator = initialComparator,
            label = label,
            reset = ::reset,
            extractor = extractor,
            initialValue = initialValue,
            initiallyEnabled = initiallyEnabled,
            visualIndicator = visualIndicator,
        ).also {
            options += it
        }

    protected fun newRatingSearchOption(
        label: String,
        initiallyEnabled: Boolean = false,
        visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
        extractor: (T) -> Rating,
    ) =
        RatingSearchOption(
            label = label,
            reset = ::reset,
            extractor = extractor,
            initiallyEnabled = initiallyEnabled,
            visualIndicator = visualIndicator,
        ).also {
            options += it
        }

    protected fun newSelectSearchOption(
        label: String,
        initiallyEnabled: Boolean = false,
        allOptions: List<String>,
        visualIndicator: VisualIndicator = VisualIndicator.NoIndicator,
        extractor: (T) -> List<String>,
    ) =
        SelectSearchOption(
            label = label,
            reset = ::reset,
            extractor = extractor,
            initiallyEnabled = initiallyEnabled,
            allOptions = allOptions,
            visualIndicator = visualIndicator,
        ).also {
            options += it
        }

    fun reset() {
        log.debug { "${this::class.simpleName} resetting search with options: ${options.joinToString { "[${it.label}/${it.enabled}]" }}" }
        predicates.clear()
        predicates.addAll(options.mapNotNull { it.buildPredicateIfEnabled() })
        resetItems()
    }

    fun matches(item: T): Boolean =
        // Vacuous truth says: even if empty, it's true :)
        predicates.all { it(item) }
}

abstract class SearchOption<T>(
    val label: String,
    protected val reset: () -> Unit,
    val permanentEnabled: Boolean,
    val visualIndicator: VisualIndicator,
) {
    private val log = logger {}
    protected val alwaysTruePredicate: (T) -> Boolean = { true }

    var enabled by mutableStateOf(permanentEnabled)
        private set

    protected abstract fun buildPredicate(): (T) -> Boolean

    fun buildPredicateIfEnabled(): ((T) -> Boolean)? =
        if (enabled) buildPredicate() else null

    open val permanentEnabledIsModified = false
    open fun resetPermanentEnabledState() {}

    fun updateEnabled(isEnabled: Boolean, suppressReset: Boolean = false) {
        if (permanentEnabled && !isEnabled) error("Cannot disable a permanently enabled search option: $this")
        if (enabled == isEnabled) return
        log.debug { "${this::class.simpleName} - updateEnabled(isEnabled=$isEnabled, suppressReset=$suppressReset)" }
        enabled = isEnabled
        if (!suppressReset) {
            reset()
        }
    }
}
