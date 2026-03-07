package seepick.localsportsclub.devApp

import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.plan.Plan
import com.github.seepick.uscclient.venue.VisitLimits
import seepick.localsportsclub.persistence.VenueDbo

object DummyGenerator {
    fun venue() = VenueDbo(
        id = 0,
        name = "",
        slug = "",
        facilities = "",
        cityId = City.Companion.Amsterdam.id,
        officialWebsite = null,
        rating = 0,
        notes = "",
        imageFileName = null,
        postalCode = "",
        street = "",
        addressLocality = "",
        latitude = "1.0",
        longitude = "2.0",
        description = "",
        importantInfo = null,
        openingTimes = null,
        isFavorited = false,
        isWishlisted = false,
        isHidden = false,
        isDeleted = false,
        isAutoSync = false,
        planId = Plan.UscPlan.Small.id,
        visitLimits = VisitLimits(small = 2, medium = 4, large = 6, xlarge = 8),
        lastSync = null,
    )
}
