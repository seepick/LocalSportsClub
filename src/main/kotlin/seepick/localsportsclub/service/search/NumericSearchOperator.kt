package seepick.localsportsclub.service.search

import seepick.localsportsclub.view.common.HasLabel

enum class NumericSearchComparator(
    override val label: String,
    val compareTo: (Number, Number) -> Boolean
) : HasLabel {
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
