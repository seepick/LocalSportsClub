package seepick.localsportsclub.service.search

import seepick.localsportsclub.view.common.HasLabel

interface NumericComparator : HasLabel {
    val compareThose: (Number, Number) -> Boolean
}

enum class FullNumericComparator(
    override val label: String,
    override val compareThose: (Number, Number) -> Boolean,
) : NumericComparator {
    Equals("=", { x, y -> x == y }),
    Not("!=", { x, y -> x != y }),
    Lower(
        "<",
        { x, y -> if (x is Int && y is Int) x < y else if (x is Double && y is Double) x < y else error("Unsupported type: ${x::class.qualifiedName}") }),
    Bigger(
        ">",
        { x, y -> if (x is Int && y is Int) x > y else if (x is Double && y is Double) x > y else error("Unsupported type: ${x::class.qualifiedName}") }),
    ;
}

enum class ComparingNumericComparator(
    override val label: String,
    override val compareThose: (Number, Number) -> Boolean,
) : NumericComparator {
    Lower(
        "<",
        { x, y -> if (x is Int && y is Int) x < y else if (x is Double && y is Double) x < y else error("Unsupported type: ${x::class.qualifiedName}") }),
    Bigger(
        ">",
        { x, y -> if (x is Int && y is Int) x > y else if (x is Double && y is Double) x > y else error("Unsupported type: ${x::class.qualifiedName}") }),
    ;
}
