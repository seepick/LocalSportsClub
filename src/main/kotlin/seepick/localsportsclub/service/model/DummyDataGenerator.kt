package seepick.localsportsclub.service.model

import io.ktor.http.Url
import seepick.localsportsclub.persistence.VenueDbo

object DummyDataGenerator {

    private val words = listOf(
        "foo", "bar", "hans", "xerox", "apple", "tree", "house", "gak", "meh",
    )
    private val suffix = listOf("v2", "ext", "beta", "super", "mega")
    private val officialWebsites = listOf("https://www.nu.nl", "https://www.ah.nl")

    fun randomVenueDbos(size: Int, customSuffix: String? = null): List<VenueDbo> =
        randomVenues(size, customSuffix).map(Venue::toDbo)

    fun randomVenues(size: Int, customSuffix: String? = null): List<Venue> =
        (1..size).map {
            val venue = randomVenue(customSuffix)
            venue.copy(id = it, slug = "$it-${venue.slug}")
        }

    private fun randomVenue(customSuffix: String? = null): Venue {
        val word = words.random()
        val suffix = customSuffix ?: if (Math.random() < 0.25) suffix.random() else null
        val number = if (Math.random() < 0.25) (1..5000).random() else null

        val fullName = word.replaceFirstChar { it.uppercase() } +
                if (suffix == null) "" else " $suffix" +
                        if (number == null) "" else " $number"
        return Venue.dummy().copy(
            name = fullName,
            slug = word + if (suffix == null) "" else "-$suffix" +
                    if (number == null) "" else "-$number",
            rating = Rating.entries.random(),
            officialWebsite = if (Math.random() < 0.70) Url(officialWebsites.random()) else null,
            notes = "Some notes for $fullName",
        )
    }

}
