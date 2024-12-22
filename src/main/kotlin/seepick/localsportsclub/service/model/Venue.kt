package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.http.Url
import seepick.localsportsclub.api.City

interface SimpleVenue {
    val id: Int
    val slug: String
    val name: String
}

data class SimpleVenueImpl(
    override val id: Int,
    override val name: String,
    override val slug: String,
) : SimpleVenue

class Venue(
    override val id: Int,
    override val name: String,
    override val slug: String,
    val description: String,
    val facilities: List<String>,
    val city: City,
    val postalCode: String,
    val street: String,
    val addressLocality: String,
    val latitude: String,
    val longitude: String,
    val imageFileName: String?,
    val importantInfo: String?,
    val openingTimes: String?,
    val uscWebsite: Url, // inferred by static URL + slug

    // TODO those down below also mutable
    val officialWebsite: Url?,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
    val isDeleted: Boolean,
    notes: String,
    rating: Rating,
    isFavorited: Boolean,
//    val linkedVenues: List<SimpleVenue>,
) : SimpleVenue {

    var notes: String by mutableStateOf(notes)
    var rating: Rating by mutableStateOf(rating)
    var isFavorited: Boolean by mutableStateOf(isFavorited)
    val activities = mutableStateListOf<Activity>()

    companion object {
        fun dummy() = Venue(
            id = 42,
            name = "Dummy Venue",
            slug = "dummy-venue",
            facilities = listOf("Gym"),
            city = City.Amsterdam,
            rating = Rating.R4,
            notes = "no notes",
            officialWebsite = null,
            description = "dummy description",
            openingTimes = null,
            importantInfo = null,
            imageFileName = null,
            uscWebsite = Url("https://usc.com/en/dummy-venue"),
            isFavorited = false,
            isWishlisted = false,
            isHidden = false,
            isDeleted = false,
            postalCode = "1001",
            street = "Street",
            addressLocality = "Amsterdam, Netherlands",
            latitude = "0.1",
            longitude = "0.2",
//            linkedVenues = emptyList(),
        )
    }

    fun copy(
        id: Int = this.id,
        name: String = this.name,
        slug: String = this.slug,
        description: String = this.description,
        facilities: List<String> = this.facilities,
        city: City = this.city,
        postalCode: String = this.postalCode,
        street: String = this.street,
        addressLocality: String = this.addressLocality,
        latitude: String = this.latitude,
        longitude: String = this.longitude,
        officialWebsite: Url? = this.officialWebsite,
        imageFileName: String? = this.imageFileName,
        importantInfo: String? = this.importantInfo,
        openingTimes: String? = this.openingTimes,
        uscWebsite: Url = this.uscWebsite,
        isWishlisted: Boolean = this.isWishlisted,
        isHidden: Boolean = this.isHidden,
        isDeleted: Boolean = this.isDeleted,
        notes: String = this.notes,
        rating: Rating = this.rating,
        isFavorited: Boolean = this.isFavorited,
    ) = Venue(
        id = id,
        name = name,
        slug = slug,
        description = description,
        facilities = facilities,
        city = city,
        postalCode = postalCode,
        street = street,
        addressLocality = addressLocality,
        latitude = latitude,
        longitude = longitude,
        officialWebsite = officialWebsite,
        imageFileName = imageFileName,
        importantInfo = importantInfo,
        openingTimes = openingTimes,
        uscWebsite = uscWebsite,
        isWishlisted = isWishlisted,
        isHidden = isHidden,
        isDeleted = isDeleted,
        notes = notes,
        rating = rating,
        isFavorited = isFavorited,
    )

    override fun toString() = "Venue[id=$id, slug=$slug, name=$name]"
}

class Rating private constructor(val value: Int) : Comparable<Rating> {
    companion object {
        private val ratingByValue by lazy {
            entries.associateBy { it.value }
        }

        fun byValue(rating: Int): Rating =
            ratingByValue[rating] ?: error("Invalid rating value: $rating")

        val R0 = Rating(0)
        val R1 = Rating(1)
        val R2 = Rating(2)
        val R3 = Rating(3)
        val R4 = Rating(4)
        val R5 = Rating(5)
        val entries = listOf(R0, R1, R2, R3, R4, R5)
    }

    val string = (0..<value).fold("") { acc, _ -> "${acc}⭐️" }
    override operator fun compareTo(other: Rating): Int =
        value.compareTo(other.value)

    override fun toString(): String = "Rating$value"
    override fun hashCode(): Int = value.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Rating) return false
        return value == other.value
    }
}
