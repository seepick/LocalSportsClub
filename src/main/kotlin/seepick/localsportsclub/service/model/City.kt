package seepick.localsportsclub.service.model

import seepick.localsportsclub.view.common.HasLabel

data class Country(
    override val label: String,
    val cities: List<City>,
) : HasLabel {
    companion object {
        val all: List<Country> by lazy { CitiesCountries.allCountries }
        fun byCityId(cityId: Int): Country =
            all.firstOrNull { it.cities.any { it.id == cityId } } ?: error("Country not found by city ID $cityId")

        fun byLabel(label: String): Country =
            all.firstOrNull { it.label == label } ?: error("Country not found by label [$label]")
    }
}

data class City(
    val id: Int,
    override val label: String,
) : HasLabel {
    companion object {
        val all: List<City> by lazy { CitiesCountries.allCountries.flatMap { it.cities } }
        val Amsterdam by lazy { byLabel("Amsterdam") }
        fun byId(cityId: Int) = all.firstOrNull { it.id == cityId } ?: error("City not found by ID $cityId")
        fun byLabel(label: String) = all.firstOrNull { it.label == label } ?: error("City not found by label [$label]")
    }
}
