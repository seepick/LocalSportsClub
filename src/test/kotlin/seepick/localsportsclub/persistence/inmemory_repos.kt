package seepick.localsportsclub.persistence

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import seepick.localsportsclub.service.model.ActivityState
import seepick.localsportsclub.service.model.FreetrainingState
import java.time.LocalDate

fun inmemoryPersistenceModule() = module {
    singleOf(::InMemoryVenueRepo) bind VenueRepo::class
    singleOf(::InMemoryVenueLinksRepo) bind VenueLinksRepo::class
    singleOf(::InMemoryActivityRepo) bind ActivityRepo::class
    singleOf(::InMemoryFreetrainingRepo) bind FreetrainingRepo::class
    singleOf(::InMemorySinglesRepo) bind SinglesRepo::class
}

class InMemoryFreetrainingRepo : FreetrainingRepo {
    val stored = mutableMapOf<Int, FreetrainingDbo>()

    override fun selectAll(cityId: Int) = stored.values.toList()
    override fun selectAllScheduled(cityId: Int) = stored.values.filter { it.isScheduled }
    override fun selectAllAnywhere(): List<FreetrainingDbo> = stored.values.toList()

    override fun selectFutureMostDate(cityId: Int): LocalDate? = stored.values.maxByOrNull { it.date }?.date
    override fun selectById(freetrainingId: Int): FreetrainingDbo? = stored[freetrainingId]

    override fun insert(dbo: FreetrainingDbo) {
        require(!stored.containsKey(dbo.id))
        stored[dbo.id] = dbo
    }

    override fun deleteNonCheckedinBefore(threshold: LocalDate): List<FreetrainingDbo> {
        val delete = stored.values.filter {
            it.state == FreetrainingState.Blank && it.date < threshold
        }
        delete.forEach {
            stored.remove(it.id)
        }
        return delete
    }

    override fun update(dbo: FreetrainingDbo) {
        require(stored.containsKey(dbo.id))
        stored[dbo.id] = dbo
    }
}

class InMemoryVenueRepo : VenueRepo {

    private var currentId = 1
    val stored = mutableMapOf<Int, VenueDbo>()

    override fun selectAllByCity(cityId: Int): List<VenueDbo> =
        stored.values.filter { it.cityId == cityId }.toList().sortedBy { it.id }

    override fun selectAllAnywhere(): List<VenueDbo> =
        stored.values.toList()

    override fun selectBySlug(slug: String): VenueDbo? = stored.values.firstOrNull { it.slug == slug }

    override fun selectById(id: Int): VenueDbo? = stored.values.firstOrNull { it.id == id }

    override fun insert(venue: VenueDbo): VenueDbo {
        val newVenue = venue.copy(id = currentId++)
        require(stored.values.none { it.slug == venue.slug })
        stored[newVenue.id] = newVenue
        return newVenue
    }

    override fun update(venue: VenueDbo): VenueDbo {
        require(stored[venue.id] != null)
        stored[venue.id] = venue
        return venue
    }
}

class InMemorySinglesRepo : SinglesRepo {
    var stored: SinglesDbo? = null
    override fun select(): SinglesDbo? = stored

    override fun insert(singles: SinglesDbo) {
        require(stored == null)
        stored = singles
    }

    override fun update(singles: SinglesDbo) {
        require(stored != null)
        stored = singles
    }
}

class InMemoryVenueLinksRepo : VenueLinksRepo {
    val stored = mutableSetOf<VenueIdLink>()
    override fun selectAll(cityId: Int): List<VenueIdLink> = stored.toList()

    override fun insert(venueIdLink: VenueIdLink) {
        stored += venueIdLink
    }
}

class InMemoryActivityRepo(
    private val venueRepo: VenueRepo? = null,
) : ActivityRepo {

    val stored = mutableMapOf<Int, ActivityDbo>()

    override fun selectAll(cityId: Int): List<ActivityDbo> =
        if (venueRepo == null) stored.values.toList()
        else {
            val venueIdsInCity = venueRepo.selectAllByCity(cityId).map { it.id }.toSet()
            stored.values.filter { venueIdsInCity.contains(it.venueId) }
        }

    override fun selectAllBooked(cityId: Int): List<ActivityDbo> {
        val condition: (ActivityDbo) -> Boolean = if (venueRepo == null) {
            { _ -> true }
        } else {
            val venueIdsInCity = venueRepo.selectAllByCity(cityId).map { it.id }.toSet();
            { activity: ActivityDbo -> venueIdsInCity.contains(activity.venueId) }
        }
        return stored.values.filter { it.state == ActivityState.Booked && condition(it) }.toList()
    }

    override fun selectAllAnywhere(): List<ActivityDbo> =
        stored.values.toList()

    override fun selectAllForVenueId(venueId: Int) = stored.values.filter { it.venueId == venueId }

    override fun selectById(id: Int): ActivityDbo? = stored[id]

    override fun selectFutureMostDate(): LocalDate? = stored.values.maxByOrNull { it.from }?.from?.toLocalDate()

    override fun selectNewestCheckedinDate(): LocalDate? =
        stored.values.filter { it.state == ActivityState.Checkedin }.maxByOrNull { it.from }?.from?.toLocalDate()

    override fun deleteBlanksBefore(threshold: LocalDate): List<ActivityDbo> {
        val deletingActivities = stored.values.filter {
            it.state == ActivityState.Blank && it.from.toLocalDate() < threshold
        }
        deletingActivities.forEach {
            stored.remove(it.id)
        }
        return deletingActivities
    }

    override fun insert(activity: ActivityDbo) {
        require(!stored.containsKey(activity.id)) { "Primary key violation: ${activity.id}" }
        stored[activity.id] = activity
    }

    override fun update(activity: ActivityDbo) {
        require(stored.containsKey(activity.id))
        stored[activity.id] = activity
    }
}
