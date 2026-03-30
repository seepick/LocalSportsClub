package seepick.localsportsclub.service.model

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.plan.Plan
import com.github.seepick.uscclient.venue.VisitLimits
import seepick.localsportsclub.service.Location
import seepick.localsportsclub.view.Lsc
import seepick.localsportsclub.view.common.HasLabel
import seepick.localsportsclub.view.common.table.TableItemAlpha
import seepick.localsportsclub.view.common.table.TableItemBgColor
import java.time.LocalDate
import java.util.TreeSet

data class Foo(
    val id: Int,
    val isNew: Boolean,
)

fun main() {
    val tree = TreeSet<Foo>(object : Comparator<Foo> {
        override fun compare(o1: Foo, o2: Foo): Int = if (o1.isNew && o2.isNew) {
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
    val categories: List<Category>,
    val city: City,
    val postalCode: String,
    val street: String,
    val addressLocality: String,
    val location: Location,
    override val distanceInKm: Double,
    val imageFileName: String?,
    val importantInfo: String?,
    val openingTimes: String?,
    val uscWebsite: String, // inferred by static URL + slug
    override val plan: Plan.UscPlan,
    // those down below also mutable...
    officialWebsite: String?,
    isDeleted: Boolean,
    notes: String,
    rating: Rating,
    isFavorited: Boolean,
    isWishlisted: Boolean,
    isHidden: Boolean,
    isAutoSync: Boolean,
    visitLimits: VisitLimits?,
    lastSync: LocalDate?,
) : HasVenue, HasLabel, HasPlan, HasDistance, TableItemBgColor, TableItemAlpha {
    override val label = name

    override val venue = this // for ScreenItem
    var notes: String by mutableStateOf(notes)
    var rating: Rating by mutableStateOf(rating)
    var isFavorited: Boolean by mutableStateOf(isFavorited)
    var isWishlisted: Boolean by mutableStateOf(isWishlisted)
    var isHidden: Boolean by mutableStateOf(isHidden)
    var isAutoSync: Boolean by mutableStateOf(isAutoSync)
    var isDeleted: Boolean by mutableStateOf(isDeleted)
    var officialWebsite: String? by mutableStateOf(officialWebsite)
    val linkedVenues = mutableStateListOf<Venue>()
    var visitLimits: VisitLimits? by mutableStateOf(visitLimits)
    var lastSync: LocalDate? by mutableStateOf(lastSync)

    val activityRemarks = mutableStateListOf<ActivityRemark>()
    val teacherRemarks = mutableStateListOf<TeacherRemark>()

    val score: Score? by derivedStateOf { calcScore() }
    override val tableBgColor: Color? get() = Lsc.colors.forScore(score, this)
    override val isTransparent = isHidden

    val nameAndFavWishEmojiPrefixedAnnotated
        get() = buildAnnotatedString {
            val emoji =
                if (isFavorited) Lsc.icons.favoritedEmoji
                else if (isWishlisted) Lsc.icons.wishlistedEmoji
                else null
            val color =
                if (isFavorited) Lsc.colors.favoritedText
                else if (isWishlisted) Lsc.colors.wishlistedText
                else Color.Unspecified

            emoji?.also { append("$it ") }
            withStyle(style = SpanStyle(color = color)) {
                append(venue.name)
            }
        }
    private val mutableActivities = mutableStateListOf<Activity>()
    val activities: List<Activity> = mutableActivities
    private val mutableFreetrainings = mutableStateListOf<Freetraining>()
    val freetrainings: List<Freetraining> = mutableFreetrainings

    // reordered according to display style in simple table (first future ASC, then past DESC)
    fun sortedActivities(today: LocalDate): List<Activity> = activities.sortedWith(Activity.comparator(today)).toList()

    fun sortedFreetrainings(today: LocalDate): List<Freetraining> =
        freetrainings.sortedWith(Freetraining.comparator(today)).toList()

    fun addActivities(activities: Set<Activity>) {
        mutableActivities += activities
    }

    fun removeActivities(activities: Set<Activity>) {
        mutableActivities -= activities
    }

    fun addFreetrainings(freetrainings: Set<Freetraining>) {
        mutableFreetrainings += freetrainings
    }

    fun removeFreetrainings(freetrainings: Set<Freetraining>) {
        mutableFreetrainings -= freetrainings
    }

    fun lastVisit(): LocalDate? {
        val nope = LocalDate.of(2000, 1, 1)
        val actMax = activities.filter { it.state == ActivityState.Checkedin }
            .maxByOrNull { it.dateTimeRange }?.dateTimeRange?.from?.toLocalDate() ?: nope
        val freMax =
            freetrainings.filter { it.state == FreetrainingState.Checkedin }.maxByOrNull { it.date }?.date ?: nope
        return maxOf(actMax, freMax).let { if (it == nope) null else it }
    }

    fun copy(
        id: Int = this.id,
        slug: String = this.slug,
        name: String = this.name,
        description: String = this.description,
        categories: List<Category> = this.categories,
        city: City = this.city,
        postalCode: String = this.postalCode,
        street: String = this.street,
        addressLocality: String = this.addressLocality,
        location: Location = this.location,
        distanceInKm: Double = this.distanceInKm,
        officialWebsite: String? = this.officialWebsite,
        imageFileName: String? = this.imageFileName,
        importantInfo: String? = this.importantInfo,
        openingTimes: String? = this.openingTimes,
        uscWebsite: String = this.uscWebsite,
        isWishlisted: Boolean = this.isWishlisted,
        isFavorited: Boolean = this.isFavorited,
        isHidden: Boolean = this.isHidden,
        isAutoSync: Boolean = this.isAutoSync,
        isDeleted: Boolean = this.isDeleted,
        notes: String = this.notes,
        rating: Rating = this.rating,
        plan: Plan.UscPlan = this.plan,
        visitLimits: VisitLimits? = this.visitLimits,
        lastSync: LocalDate? = this.lastSync,
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
        isAutoSync = isAutoSync,
        isDeleted = isDeleted,
        notes = notes,
        rating = rating,
        isFavorited = isFavorited,
        plan = plan,
        visitLimits = visitLimits,
        lastSync = lastSync,
    )

    override fun toString() = "Venue[id=$id, slug=$slug, name=$name, rating=$rating]"
    override fun hashCode() = slug.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is Venue) return false
        return id == other.id && slug == other.slug && name == other.name && description == other.description && categories == other.categories && city == other.city &&
//                name == other.name &&
                notes == other.notes && imageFileName == other.imageFileName && isFavorited == other.isFavorited && rating == other.rating
    }

    object Ids {
        val EmsHealthStudio = 106
        val HotFlowYogaJordaan = 178
        val MassageSchoolItmThaiHandAmsterdam = 217
        val MovementAmsterdam = 233
        val MovementCity = 235
        val RelaxLoungOvertoom = 277
    }
}

enum class Rating(val value: Int) : Comparable<Rating>, HasLabel {
    R0(0),
    R1(1),
    R2(2),
    R3(3),
    R4(4),
    R5(5);

    companion object {
        private val ratingByValue by lazy {
            entries.associateBy { it.value }
        }

        fun byValue(rating: Int): Rating = ratingByValue[rating] ?: error("Invalid rating value: $rating")
    }

    override val label = (0..<value).fold("") { acc, _ -> "${acc}${Lsc.icons.ratingEmoji}" }

    override fun toString(): String = "Rating$value"
}
