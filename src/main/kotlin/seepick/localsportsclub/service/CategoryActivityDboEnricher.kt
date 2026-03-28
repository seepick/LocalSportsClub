package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.ActivityDbo
import seepick.localsportsclub.service.model.Venue

/*
select a.NAME, a.CATEGORY, v.name from ACTIVITIES a
join VENUES v on a.VENUE_ID = v.ID
where lower(a.NAME) like '%pilates%' and a.CATEGORY != 'Pilates'
group by a.NAME, a.CATEGORY

select a.NAME, a.CATEGORY, v.name from ACTIVITIES a
join VENUES v on a.VENUE_ID = v.ID
where a.STATE != 'Blank'
group by a.NAME, a.CATEGORY
 */
class CategoryActivityDboEnricher : ActivityDboEnricher {

    override fun enrich(dbo: ActivityDbo): ActivityDbo {
        // @formatter:off
        if ((dbo.name == "HOT C" && dbo.category == "Fitness") ||
            (dbo.name == "Energizing Yoga" && dbo.category == "Fitness") ||
            (dbo.name.contains("yoga") && dbo.category != "Yoga")
        ) {
            return dbo.copy(category = "Yoga")
        }

        if (
            (dbo.name.equals("pilates", ignoreCase = true) && dbo.category != "Pilates") ||
            (dbo.name.contains("pilates", ignoreCase = true) && dbo.category != "Pilates") ||
            (dbo.name == "CorePower" && dbo.category == "Yoga")
        ) {
            return dbo.copy(category = "Pilates")
        }

        if (
            (dbo.name.contains("reformer", ignoreCase = true) && dbo.category == "Pilates")
        ) {
            return dbo.copy(category = "Pilates Reformer")
        }

        if (
            (dbo.name.equals("acro yoga", ignoreCase = true) && dbo.category == "Yoga") ||
            (dbo.name.equals("acroyoga", ignoreCase = true) && dbo.category == "Yoga") ||
            (dbo.venueId == Venue.Ids.MovementAmsterdam)
        ) {
            return dbo.copy(category = "Functional Training")
        }

        if (
            (dbo.name.contains("yin", ignoreCase = true) && (dbo.category == "Yoga" || dbo.category == "Meditation")) ||
            (dbo.name.contains("restoratieve", ignoreCase = true) && (dbo.category == "Yoga" || dbo.category == "Meditation")) ||
            (dbo.name.contains("restorative", ignoreCase = true) && (dbo.category == "Yoga" || dbo.category == "Meditation")) ||
            (dbo.name.contains("sound", ignoreCase = true) && (dbo.category == "Yoga" || dbo.category == "Meditation"))
        ) {
            return dbo.copy(category = "Relaxation")
        }

        if (dbo.venueId == Venue.Ids.MassageSchoolItmThaiHandAmsterdam ||
            dbo.venueId == Venue.Ids.RelaxLoungOvertoom
        ) {
            return dbo.copy(category = "Massage")
        }

        if (dbo.name.contains("essentrics", ignoreCase = true) && dbo.category != "Fitness") {
            return dbo.copy(category = "Fitness")
        }

        if (dbo.name.contains("taichi", ignoreCase = true) && dbo.category != "Qi Gong and Tai Chi") {
            return dbo.copy(category = "Qi Gong and Tai Chi")
        }

        if (dbo.name.contains("zumba", ignoreCase = true) && dbo.category != "Dance") {
            return dbo.copy(category = "Dancing")
        }
        if ((dbo.name.contains("breathwork", ignoreCase = true) && dbo.category != "Meditation")
        ) {
            return dbo.copy(category = "Meditation")
        }

        if (dbo.venueId == Venue.Ids.EmsHealthStudio) {
            return dbo.copy(category = "EMS")
        }

        return dbo
        // @formatter:on
    }
}
