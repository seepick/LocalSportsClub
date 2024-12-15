package seepick.localsportsclub.api

enum class City(val id: Int) {
    Amsterdam(1144)
}

enum class District(val label: String, val id: Int, val parent: Int?) {
    Amsterdam("Amsterdam", 8749, null),
    Amsterdam_Centrum("Centrum", 8777, Amsterdam.id),
}

enum class PlanType(val id: Int, val label: String) {
    Small(1, "S"),
    Medium(2, "M"),
    Large(3, "L"),
    ExtraLarge(6, "XL"),
}
