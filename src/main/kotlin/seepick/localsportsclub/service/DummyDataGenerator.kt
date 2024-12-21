package seepick.localsportsclub.service

import seepick.localsportsclub.api.domain.Rating
import seepick.localsportsclub.api.domain.Venue
import java.net.URI

object DummyDataGenerator {

    private val words = listOf(
        "foo", "bar", "hans", "xerox", "apple", "tree", "house", "gak", "meh",
    )
    private val suffix = listOf("v2", "ext", "beta", "super", "mega")
    private val officialWebsites = listOf("https://www.nu.nl", "https://www.ah.nl")

    fun generateVenues(size: Int, customSuffix: String? = null): List<Venue> =
        (1..size).map {
            val venue = generateVenue(customSuffix)
            venue.copy(id = it, slug = "$it-${venue.slug}")
        }

    private fun generateVenue(customSuffix: String? = null): Venue {
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
            officialWebsite = if (Math.random() < 0.70) URI(officialWebsites.random()) else null,
            notes = "Some notes for $fullName",
        )
    }

}
