package seepick.localsportsclub.api.domain

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.api.City
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenuesRepo

interface VenuesService {
    fun insert()
    fun select()
}

class VenuesServiceImpl(
    private val venuesRepo: VenuesRepo
) : VenuesService {

    private val log = logger { }

    override fun insert() {
        log.info { "insert()" }
        transaction {
            val nextId = (venuesRepo.selectAll().maxOfOrNull { it.id } ?: 0) + 1
            venuesRepo.persist(
                listOf(
                    VenueDbo(
                        id = nextId,
                        name = "name $nextId",
                        slug = "slug${nextId}",
                        facilities = "Gym",
                        cityId = City.Amsterdam.id,
                        officialWebsite = null,
                        rating = 3,
                        note = "some note",
                        isFavorited = false,
                        isWishlisted = false,
                        isHidden = false,
                        isDeleted = false,
                    )
                )
            )
        }
    }

    override fun select() {
        log.info { "select()" }
        val venues = transaction {
            venuesRepo.selectAll()
        }
        log.debug { "Found ${venues.size} partners in DB." }
        venues.forEach {
            println(it)
        }
    }
}
