package seepick.localsportsclub.api

enum class City(val id: Int, val label: String) {
    Amsterdam(1144, "Amsterdam"),
    Breda(1158, "Breda"),
    Haarlem(1146, "Haarlem"),
    ;

    companion object {
        private val cityById by lazy {
            entries.associateBy { it.id }
        }

        fun byId(cityId: Int): City =
            cityById[cityId] ?: error("Invalid city ID: $cityId")
    }
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
