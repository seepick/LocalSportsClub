package seepick.localsportsclub.service.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import seepick.localsportsclub.Lsc
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.view.common.HasLabel
import seepick.localsportsclub.view.common.table.TableItemBgColor
import java.time.LocalDate
import java.util.TreeSet

data class Foo(
    val id: Int,
    val isNew: Boolean,
)

fun main() {
    val tree = TreeSet<Foo>(object : Comparator<Foo> {
        override fun compare(o1: Foo, o2: Foo): Int =
            if (o1.isNew && o2.isNew) {
                o1.id.compareTo(o2.id)
            } else if (!o1.isNew && !o2.isNew) {
                o2.id.compareTo(o1.id)
            } else {
                if (o1.isNew) -1 else 1
            }
    })
    tree.add(Foo(3, true))
    tree.add(Foo(4, false))
    tree.add(Foo(5, true))
    tree.add(Foo(1, false))
    tree.add(Foo(2, true))
    println(tree)
}

class Venue(
    val id: Int,
    val name: String,
    val slug: String,
    val description: String,
    val categories: List<String>,
    val city: City,
    val postalCode: String,
    val street: String,
    val addressLocality: String,
    val location: Location?,
    override val distanceInKm: Double?,
    val imageFileName: String?,
    val importantInfo: String?,
    val openingTimes: String?,
    val uscWebsite: String, // inferred by static URL + slug
    val plan: Plan.UscPlan,
    // those down below also mutable...
    officialWebsite: String?,
    val isDeleted: Boolean,
    notes: String,
    rating: Rating,
    isFavorited: Boolean,
    isWishlisted: Boolean,
    isHidden: Boolean,
) : HasVenue, HasLabel, HasDistance, TableItemBgColor {
    override val label = name
    override val tableBgColor get() = computeBgColor(isFavorited = isFavorited, isWishlisted = isWishlisted)

    //  val linkedVenues: MutableList<Venue>,
    override val venue = this // for ScreenItem
    var notes: String by mutableStateOf(notes)
    var rating: Rating by mutableStateOf(rating)
    var isFavorited: Boolean by mutableStateOf(isFavorited)
    var isWishlisted: Boolean by mutableStateOf(isWishlisted)
    var isHidden: Boolean by mutableStateOf(isHidden)
    var officialWebsite: String? by mutableStateOf(officialWebsite)
    val linkedVenues = mutableStateListOf<Venue>()

    private val _activities = mutableStateListOf<Activity>()
    val activities: List<Activity> = _activities
    private val _freetrainings = mutableStateListOf<Freetraining>()
    val freetrainings: List<Freetraining> = _freetrainings

    private val now = LocalDate.now()

    // reordered according to display style in simple table (first future ASC, then past DESC)
    val sortedActivities = TreeSet(Comparator<Activity> { a1, a2 ->
        val a1IsNew = a1.dateTimeRange.from.toLocalDate() >= now
        val a2IsNew = a2.dateTimeRange.from.toLocalDate() >= now
        if (a1IsNew && a2IsNew) {
            a1.dateTimeRange.from.compareTo(a2.dateTimeRange.from)
        } else if (!a1IsNew && !a2IsNew) {
            a2.dateTimeRange.from.compareTo(a1.dateTimeRange.from)
        } else {
            if (a1IsNew) -1 else 1
        }
    })
    val sortedFreetrainings = TreeSet(Comparator<Freetraining> { f1, f2 ->
        val f1IsNew = f1.date >= now
        val f2IsNew = f2.date >= now
        if (f1IsNew && f2IsNew) {
            f1.date.compareTo(f2.date)
        } else if (!f1IsNew && !f2IsNew) {
            f2.date.compareTo(f1.date)
        } else {
            if (f1IsNew) -1 else 1
        }
    })

    fun addActivities(activities: Set<Activity>) {
        _activities += activities
        sortedActivities += activities
    }

    fun removeActivities(activities: Set<Activity>) {
        _activities -= activities
        sortedActivities -= activities
    }

    fun addFreetrainings(freetrainings: Set<Freetraining>) {
        _freetrainings += freetrainings
        sortedFreetrainings += freetrainings
    }

    fun removeFreetrainings(freetrainings: Set<Freetraining>) {
        _freetrainings -= freetrainings
        sortedFreetrainings -= freetrainings
    }

    fun lastVisit(): LocalDate? {
        val nope = LocalDate.of(2000, 1, 1)
        val actMax = _activities.filter { it.state == ActivityState.Checkedin }
            .maxByOrNull { it.dateTimeRange }?.dateTimeRange?.from?.toLocalDate() ?: nope
        val freMax =
            freetrainings.filter { it.state == FreetrainingState.Checkedin }.maxByOrNull { it.date }?.date ?: nope
        return maxOf(actMax, freMax).let { if (it == nope) null else it }
    }

    companion object {
        private fun computeBgColor(
            isFavorited: Boolean,
            isWishlisted: Boolean,
        ): Color? =
            if (isFavorited) Lsc.colors.isFavorited else if (isWishlisted) Lsc.colors.isWishlisted else null

        fun dummy() = Venue(
            id = 42,
            name = "Dummy Venue",
            slug = "dummy-venue",
            categories = listOf("Gym"),
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
            location = null,
            distanceInKm = null,
            plan = Plan.UscPlan.Small,
        )
    }

    fun copy(
        id: Int = this.id,
        slug: String = this.slug,
        name: String = this.name,
        description: String = this.description,
        categories: List<String> = this.categories,
        city: City = this.city,
        postalCode: String = this.postalCode,
        street: String = this.street,
        addressLocality: String = this.addressLocality,
        location: Location? = this.location,
        distanceInKm: Double? = this.distanceInKm,
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
        linkedVenues: List<Venue> = this.linkedVenues,
        plan: Plan.UscPlan = this.plan,
    ) = Venue(
        id = id,
        slug = slug,
        name = name,
        description = description,
        categories = categories,
        city = city,
        postalCode = postalCode,
        street = street,
        addressLocality = addressLocality,
        location = location,
        distanceInKm = distanceInKm,
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
        plan = plan,
    )

    override fun toString() = "Venue[id=$id, slug=$slug, name=$name, rating=$rating]"
    override fun hashCode() = slug.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Venue) return false
        return id == other.id && slug == other.slug && name == other.name && description == other.description && categories == other.categories && city == other.city &&
//                name == other.name &&
                notes == other.notes && imageFileName == other.imageFileName && isFavorited == other.isFavorited && rating == other.rating
    }
}

class Rating private constructor(val value: Int) : Comparable<Rating>, HasLabel {
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

    override val label = (0..<value).fold("") { acc, _ -> "${acc}${Lsc.icons.ratingEmoji}" }
    override operator fun compareTo(other: Rating): Int = value.compareTo(other.value)

    override fun toString(): String = "Rating$value"
    override fun hashCode(): Int = value.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Rating) return false
        return value == other.value
    }
}
