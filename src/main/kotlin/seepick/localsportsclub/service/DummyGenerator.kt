package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Plan

object DummyGenerator {

    fun venue() = VenueDbo(
        id = 0,
        name = "",
        slug = "",
        facilities = "",
        cityId = City.Amsterdam.id,
        officialWebsite = null,
        rating = 0,
        notes = "",
        imageFileName = null,
        postalCode = "",
        street = "",
        addressLocality = "",
        latitude = "",
        longitude = "",
        description = "",
        importantInfo = null,
        openingTimes = null,
        isFavorited = false,
        isWishlisted = false,
        isHidden = false,
        isDeleted = false,
        planId = Plan.UscPlan.Small.id,
    )
}
