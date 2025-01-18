package seepick.localsportsclub.service.model

import seepick.localsportsclub.view.common.HasLabel

data class Country(
    override val label: String,
    val cities: List<City>,
) : HasLabel {
    companion object {
        val all: List<Country> by lazy { CitiesCountries.allCountries }
        fun byCityId(cityId: Int): Country = all.single { it.cities.any { it.id == cityId } }
    }
}

data class City(
    val id: Int,
    override val label: String,
) : HasLabel {
    companion object {
        val all: List<City> by lazy { CitiesCountries.allCities }
        val Amsterdam = City(1144, "Amsterdam")
        fun byId(cityId: Int) = CitiesCountries.cityById(cityId)
    }
}

//enum class City(val id: Int, val label: String) {
//    Amsterdam(1144, "Amsterdam"),
//    Breda(1158, "Breda"),
//    Haarlem(1146, "Haarlem"),
//    ;
//
//    companion object {
//        private val cityById by lazy {
//            entries.associateBy { it.id }
//        }
//
//        fun byId(cityId: Int): City =
//            cityById[cityId] ?: error("Invalid city ID: $cityId")
//    }
//}
