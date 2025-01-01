package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.api.City
import seepick.localsportsclub.view.shared.ScreenItem

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
    override val imageFileName: String?,
    val importantInfo: String?,
    val openingTimes: String?,
    val uscWebsite: String, // inferred by static URL + slug

    // those down below also mutable...
    officialWebsite: String?,
    val isDeleted: Boolean,
    notes: String,
    rating: Rating,
    isFavorited: Boolean,
    isWishlisted: Boolean,
    isHidden: Boolean,
//    val linkedVenues: List<SimpleVenue>,
) : SimpleVenue, ScreenItem {

    override val venue = this
    var notes: String by mutableStateOf(notes)
    override var rating: Rating by mutableStateOf(rating)
    override var isFavorited: Boolean by mutableStateOf(isFavorited)
    override var isWishlisted: Boolean by mutableStateOf(isWishlisted)
    override var isHidden: Boolean by mutableStateOf(isHidden)
    val activities = mutableStateListOf<Activity>()
    val freetrainings = mutableStateListOf<Freetraining>()
    var officialWebsite: String? by mutableStateOf(officialWebsite)
    val checkinsCount = activities.filter { it.wasCheckedin }.size + freetrainings.filter { it.wasCheckedin }.size

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
            uscWebsite = "https://usc.com/en/dummy-venue",
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
        slug: String = this.slug,
        name: String = this.name,
        description: String = this.description,
        facilities: List<String> = this.facilities,
        city: City = this.city,
        postalCode: String = this.postalCode,
        street: String = this.street,
        addressLocality: String = this.addressLocality,
        latitude: String = this.latitude,
        longitude: String = this.longitude,
        officialWebsite: String? = this.officialWebsite,
        imageFileName: String? = this.imageFileName,
        importantInfo: String? = this.importantInfo,
        openingTimes: String? = this.openingTimes,
        uscWebsite: String = this.uscWebsite,
        isWishlisted: Boolean = this.isWishlisted,
        isHidden: Boolean = this.isHidden,
        isDeleted: Boolean = this.isDeleted,
        notes: String = this.notes,
        rating: Rating = this.rating,
        isFavorited: Boolean = this.isFavorited,
    ) = Venue(
        id = id,
        slug = slug,
        name = name,
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

    override fun toString() = "Venue[id=$id, slug=$slug, name=$name, rating=$rating]"
    override fun hashCode() = slug.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Venue) return false
        return id == other.id && slug == other.slug && name == other.name && description == other.description && facilities == other.facilities && city == other.city &&
//                name == other.name &&
                notes == other.notes && imageFileName == other.imageFileName && isFavorited == other.isFavorited && rating == other.rating
    }
}

class Rating private constructor(val value: Int) : Comparable<Rating> {
    companion object {
        private val ratingByValue by lazy {
            entries.associateBy { it.value }
        }

        fun byValue(rating: Int): Rating = ratingByValue[rating] ?: error("Invalid rating value: $rating")

        val R0 = Rating(0)
        val R1 = Rating(1)
        val R2 = Rating(2)
        val R3 = Rating(3)
        val R4 = Rating(4)
        val R5 = Rating(5)
        val entries = listOf(R0, R1, R2, R3, R4, R5)
    }

    val string = (0..<value).fold("") { acc, _ -> "${acc}⭐️" }
    override operator fun compareTo(other: Rating): Int = value.compareTo(other.value)

    override fun toString(): String = "Rating$value"
    override fun hashCode(): Int = value.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Rating) return false
        return value == other.value
    }
}
