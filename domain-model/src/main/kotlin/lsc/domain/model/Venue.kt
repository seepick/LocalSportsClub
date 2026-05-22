package lsc.domain.model

import lsc.domain.model.usc.City
import lsc.domain.model.usc.Plan
import lsc.domain.model.usc.VisitLimits
import java.time.LocalDate

// TODO restore interfaces?
data class Venue(
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
    val distanceInKm: Double,
    val imageFileName: String?,
    val importantInfo: String?,
    val openingTimes: String?,
    val uscWebsite: String, // inferred by static URL + slug
    val plan: Plan.UscPlan,
    val createdAt: LocalDate,
    val deletedAt: LocalDate?,
    // those down below also mutable...
    val officialWebsite: String?,
    val isDeleted: Boolean,
    val notes: String,
    val rating: Rating,
    val isFavorited: Boolean,
    val isWishlisted: Boolean,
    val isHidden: Boolean,
    val isAutoSync: Boolean,
    val visitLimits: VisitLimits?,
    val lastSync: LocalDate?,
)

data class Category(
    val name: String,
    val rating: RemarkRating?,
) : Comparable<Category> {
    override fun compareTo(other: Category) = name.compareTo(other.name)
}

enum class RemarkRating(
    val numericValue: Int,
    val weightedValue: Int,
) {
    Amazing(3, 3),
    Good(2, 2),
    Meh(1, -1),
    Bad(0, -3),
    ;

    companion object {
        val default = Good
    }
}
