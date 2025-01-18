package seepick.localsportsclub.service.model

private val _allCities by lazy {
    CitiesCountries.allCountries.flatMap { it.cities }
}

fun CitiesCountries.cityById(cityId: Int): City =
    allCities.firstOrNull { it.id == cityId } ?: error("City not found by ID $cityId")

val CitiesCountries.allCities get(): List<City> = _allCities
