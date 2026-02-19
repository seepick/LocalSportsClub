package seepick.localsportsclub.tools.migration

import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.plan.Plan
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.ExposedActivityRepo
import seepick.localsportsclub.persistence.ExposedFreetrainingRepo
import seepick.localsportsclub.persistence.ExposedVenueLinksRepo
import seepick.localsportsclub.persistence.ExposedVenueRepo
import seepick.localsportsclub.persistence.FreetrainingDbo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueIdLink
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object MigrationProcessor {

    private var currentActivityId = 1000
    private var currentFreetrainingId = 1000
    private val onefitNameMarker = "[OneFit]"

    fun process(matches: List<MigrationMatch>, venues: List<VenueDbo>) {
        matches.forEach { match ->
            when (match.matchType) {
                is MatchType.CreateDeletedVenue -> {
                    println("Creating venue locally marked as deleted: ${match.partner.name}")
                    insertDeletedVenue(match.partner, match.matchType.createMapping.linkedVenueSlugs, venues)
                }

                is MatchType.LinkToExistingVenue -> {
                    println("Linking existing: ${match.matchType.venueDbo}")
                    updateExistingVenue(match.partner, match.matchType.venueDbo)
                }
            }
        }
    }

    private fun insertDeletedVenue(partner: OnefitPartner, linkedVenueSlugs: List<String>, venues: List<VenueDbo>) {
        var postalCode = ""
        var street = ""
        var addressLocality = ""
        var latitude = "0.0"
        var longitude = "0.0"
        if (partner.locations.isNotEmpty()) {
            val loc = partner.locations.first()
            postalCode = loc.zipCode
            addressLocality = loc.city
            street = "${loc.streetName} ${loc.houseNumber} ${loc.addition}"
            latitude = loc.latitude.toString()
            longitude = loc.longitude.toString()
        }
        val dbo = VenueDbo(
            id = -1,
            name = "${partner.name} $onefitNameMarker",
            slug = buildSlug(partner.name),
            facilities = "",
            cityId = City.Amsterdam.id,
            officialWebsite = partner.website,
            rating = partner.rating,
            notes = partner.note,
            imageFileName = null,
            postalCode = postalCode,
            street = street,
            addressLocality = addressLocality,
            latitude = latitude,
            longitude = longitude,
            description = "",
            importantInfo = null,
            openingTimes = null,
            isFavorited = partner.isFavorited,
            isWishlisted = partner.isWishlisted,
            isHidden = partner.isHidden,
            isDeleted = true,
            isAutoSync = false,
            planId = Plan.UscPlan.Small.id,
        )
        val insertedVenueId = ExposedVenueRepo.insert(dbo).id
        linkedVenueSlugs.forEach { otherSlug ->
            ExposedVenueLinksRepo.insert(VenueIdLink(insertedVenueId, venues.single { it.slug == otherSlug }.id))
        }
        insertCheckinsAndDropins(partner, insertedVenueId)
    }

    private fun updateExistingVenue(partner: OnefitPartner, venueDbo: VenueDbo) {
        ExposedVenueRepo.update(
            venueDbo.copy(
                rating = partner.rating,
                notes = partner.note.let { if (it.isEmpty()) "" else "[AllFit] says for ${partner.name}: ${partner.note}" },
                officialWebsite = partner.website ?: venueDbo.officialWebsite,
                isWishlisted = partner.isWishlisted,
                isFavorited = partner.isFavorited,
                isHidden = partner.isHidden,
            )
        )
        insertCheckinsAndDropins(partner, venueDbo.id)
    }

    private fun insertCheckinsAndDropins(partner: OnefitPartner, venueId: Int) {
        insertCheckins(partner.checkins, venueId)
        insertDropins(partner.dropins, venueId)
    }

    private fun insertCheckins(checkins: List<MigrationCheckin>, venueId: Int) {
        checkins.forEach { checkin ->
            ExposedActivityRepo.insert(
                ActivityDbo(
                    id = currentActivityId++,
                    venueId = venueId,
                    name = "${checkin.workoutName} $onefitNameMarker",
                    category = "",
                    spotsLeft = 0,
                    from = parseZonedDateTimeToLocalDateTime(checkin.start),
                    to = parseZonedDateTimeToLocalDateTime(checkin.end),
                    teacher = null,
                    description = null,
                    state = ActivityState.Checkedin,
                    cancellationLimit = null,
                    planId = Plan.UscPlan.Small.id,
                )
            )
        }
    }

    private fun insertDropins(dropins: List<MigrationDropins>, venueId: Int) {
        dropins.forEach { dropin ->
            ExposedFreetrainingRepo.insert(
                FreetrainingDbo(
                    id = currentFreetrainingId++,
                    venueId = venueId,
                    name = "DropIn $onefitNameMarker",
                    category = "",
                    date = parseZonedDateTimeToLocalDateTime(dropin.createdAt).toLocalDate(),
                    state = FreetrainingState.Checkedin,
                    planId = Plan.UscPlan.Small.id,
                )
            )
        }
    }
}

private fun parseZonedDateTimeToLocalDateTime(string: String): LocalDateTime =
    LocalDateTime.from(DateTimeFormatter.ISO_DATE_TIME.parse(string))

private val duplicates = "__+".toRegex()

private fun buildSlug(name: String): String =
    name.map { if (it.isLetterOrDigit()) it else '_' }.joinToString("")
        .lowercase()
        .replace(duplicates, "_")
