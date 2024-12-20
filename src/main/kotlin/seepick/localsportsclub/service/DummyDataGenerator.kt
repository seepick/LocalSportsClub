package seepick.localsportsclub.service

import seepick.localsportsclub.api.domain.Rating
import seepick.localsportsclub.api.domain.Venue

object DummyDataGenerator {

    private val words = listOf(
        "foo", "bar", "hans", "xerox", "apple", "tree", "house", "gak", "meh",
    )
    private val suffix = listOf("v2", "ext", "beta", "super", "mega")

    fun generateVenue(customSuffix: String? = null): Venue {
        val word = words.random()
        val suffix = customSuffix ?: if (Math.random() < 0.25) suffix.random() else null
        val number = if (Math.random() < 0.25) (1..5000).random() else null

        return Venue.dummy().copy(
            name = word.replaceFirstChar { it.uppercase() } +
                    if (suffix == null) "" else " $suffix" +
                            if (number == null) "" else " $number",
            slug = word + if (suffix == null) "" else "-$suffix" +
                    if (number == null) "" else "-$number",
            rating = Rating.values.random(),
        )
    }

    fun generateVenues(size: Int, customSuffix: String? = null): List<Venue> =
        (1..size).map {
            generateVenue(customSuffix).copy(id = it)
        }
}
