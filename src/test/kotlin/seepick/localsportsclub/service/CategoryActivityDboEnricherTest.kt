package seepick.localsportsclub.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.persistence.activityDbo
import seepick.localsportsclub.service.model.Venue

class CategoryActivityDboEnricherTest : StringSpec({
    val originalCategory = "original category"
    val anyVenue = 9999

    fun newActivity(
        name: String = "original name",
        category: String = originalCategory,
        venueId: Int = anyVenue,
    ): ActivityDbo {
        return Arb.activityDbo().next().copy(
            name = name,
            category = category,
            venueId = venueId,
        )
    }

    listOf(
        newActivity(name = "Energizing Yoga", category = "Fitness") to "Yoga",
        newActivity(name = "HOT C", category = "Fitness") to "Yoga",

        newActivity(name = "Hot Pilates Fusion", category = "Yoga") to "Pilates",
        newActivity(name = "Pilates") to "Pilates",
        newActivity(name = "PiLaTeS") to "Pilates",
        newActivity(name = "--Pilates--", category = "Yoga") to "Pilates",
        newActivity(name = "CorePower", category = "Yoga") to "Pilates",

        newActivity(name = "Acro Yoga", category = "Yoga") to "Functional Training",
        newActivity(name = "Acroyoga", category = "Yoga") to "Functional Training",
        newActivity(venueId = Venue.Ids.MovementAmsterdam) to "Functional Training",

        newActivity(name = "--yin--", category = "Yoga") to "Relaxation",
        newActivity(name = "RESTORATIVE YOGA", category = "Yoga") to "Relaxation",
        newActivity(name = "--restoratieve--", category = "Yoga") to "Relaxation",
        newActivity(name = "--restorative--", category = "Yoga") to "Relaxation",
        newActivity(name = "--sound--", category = "Yoga") to "Relaxation",

        newActivity(venueId = Venue.Ids.RelaxLoungOvertoom) to "Massage",
        newActivity(venueId = Venue.Ids.MassageSchoolItmThaiHandAmsterdam) to "Massage",

        newActivity(venueId = Venue.Ids.EmsHealthStudio) to "EMS",
    )
        .forEach { (dbo, expectedCategory) ->
            "enrich should return '$expectedCategory' for '${dbo.name}/${dbo.category}/${dbo.venueId}'" {
                CategoryActivityDboEnricher().enrich(dbo).category shouldBe expectedCategory
            }
        }
})
