package seepick.localsportsclub.tools

import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.api.activity.cleanActivityFreetrainingName
import seepick.localsportsclub.api.venue.cleanVenueInfo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.ExposedActivityRepo
import seepick.localsportsclub.persistence.ExposedFreetrainingRepo
import seepick.localsportsclub.persistence.ExposedVenueLinksRepo
import seepick.localsportsclub.persistence.ExposedVenueRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueIdLink
import seepick.localsportsclub.persistence.VenueLinksRepo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.unescape

object DataCleaner {

    private const val prodMode = true

    @JvmStatic
    fun main(args: Array<String>) {
        connectToDatabase(isProd = prodMode)
        if (prodMode) {
            println("You are about to apply changes to PRODUCTION. Are you sure?")
            print("[y|N]>> ")
            val read = readln()
            if (read != "y") {
                println("Aborted...")
                return
            }
        }

        cleanActivityNames()
//        cleanVenueAddresses()
//        cleanTexts()
//        linkMissingVenues()
//        changeVenues()
        println("Done ✅")
    }

    private fun changeVenues() {
        listOf("ems-health-studio", "ems-health-studio-groepslessen").forEach { slug ->
            val venue = venueRepo.selectBySlug(slug)!!
            venueRepo.update(venue.copy(facilities = "EMS"))
        }
    }

    private val activityRepo: ActivityRepo = ExposedActivityRepo
    private val freetrainingRepo: FreetrainingRepo = ExposedFreetrainingRepo
    private val venueRepo: VenueRepo = ExposedVenueRepo
    private val linkRepo: VenueLinksRepo = ExposedVenueLinksRepo

    enum class VenueDboText {
        description {
            override fun getValue(venueDbo: VenueDbo) = venueDbo.description
            override fun updateByCopy(venueDbo: VenueDbo, value: String) = venueDbo.copy(description = value)
        },
        openingTimes {
            override fun getValue(venueDbo: VenueDbo) = venueDbo.openingTimes
            override fun updateByCopy(venueDbo: VenueDbo, value: String) = venueDbo.copy(openingTimes = value)
        },
        importantInfo {
            override fun getValue(venueDbo: VenueDbo) = venueDbo.importantInfo
            override fun updateByCopy(venueDbo: VenueDbo, value: String) = venueDbo.copy(importantInfo = value)
        };

        abstract fun getValue(venueDbo: VenueDbo): String?
        abstract fun updateByCopy(venueDbo: VenueDbo, value: String): VenueDbo
    }

    private fun cleanTexts() {
        val escapedSymbols = listOf("\\\"", "\\n")
        venueRepo.selectAll().forEach { venue ->
            val dirtyTexts = VenueDboText.entries.filter { textField ->
                val value: String? = textField.getValue(venue)
                (value != null && escapedSymbols.any { value.contains(it) })
            }
            if (dirtyTexts.isNotEmpty()) {
                var cleanedVenue = venue
                dirtyTexts.forEach {
                    val cleanedValue = it.getValue(cleanedVenue)
                    if (cleanedValue != null) {
                        cleanedVenue = it.updateByCopy(cleanedVenue, cleanedValue.unescape())
                    }
                }
                venueRepo.update(cleanedVenue)
            }
        }

        venueRepo.selectAll().forEach { venue ->
            if (venue.importantInfo != null) {
                val cleanedInfo = cleanVenueInfo(venue.importantInfo)
                if (cleanedInfo != venue.importantInfo) {
                    venueRepo.update(venue.copy(importantInfo = cleanedInfo))
                }
            }
        }
    }


    private fun cleanActivityNames() = transaction {
        activityRepo.selectAll().forEach {
            val cleaned = cleanActivityFreetrainingName(it.name)
            if (it.name != cleaned) {
                println("[${it.name}] => [$cleaned]")
                activityRepo.update(it.copy(name = cleaned))
            }
        }
        freetrainingRepo.selectAll().forEach {
            val cleaned = cleanActivityFreetrainingName(it.name)
            if (it.name != cleaned) {
                println("[${it.name}] => [$cleaned]")
                freetrainingRepo.update(it.copy(name = cleaned))
            }
        }
    }

    private fun cleanVenueAddresses() = transaction {
        venueRepo.selectBySlug("vondelpark-rosepark-rosarium")!!.also { venue ->
            venueRepo.update(
                venue.copy(
                    postalCode = "1071 AL", notes = "Coordinates: 52°21'27.6\"N 4°51'46.7\"E"
                )
            )
        }
        venueRepo.selectBySlug("fitness-academy-amsterdam-olvg")!!.also { venue ->
            venueRepo.update(venue.copy(street = "'s-Gravesandeplein"))
        }
        venueRepo.selectBySlug("studio_balance")!!.also { venue ->
            venueRepo.update(
                venue.copy(
                    street = "Rijnstraat 63",
                    postalCode = "1079 GW",
                    addressLocality = "Amsterdam, Netherlands",
                    latitude = "52.3459377",
                    longitude = "4.9056754"
                )
            )
        }
        venueRepo.selectBySlug("more-real-progression")!!.also { venue ->
            venueRepo.update(venue.copy(street = "'s-Gravesandeplein 8"))
        }
        venueRepo.selectBySlug("centrum")!!.also { venue ->
            venueRepo.update(venue.copy(street = "Jodenbreestraat 25"))
        }
        venueRepo.selectBySlug("powerwalk-amsterdam-sloterpark")!!.also { venue ->
            venueRepo.update(
                venue.copy(
                    notes = "buiten bij de bakfiets met zwart dekzeil",
                    street = "Dokter Meurerlaan 7",
                )
            )
        }
        venueRepo.selectAll().filter { it.street == "undefined" }.forEach {
            venueRepo.update(it.copy(street = ""))
        }
        venueRepo.selectAll().filter { it.addressLocality == "Amsterdam" }.forEach {
            venueRepo.update(it.copy(addressLocality = "Amsterdam, Netherlands"))
        }
    }

    private fun linkMissingVenues() {
        if (prodMode) error("This is NOT necessary for PROD anymore, as syncer was already fixed!")
        val links = linkRepo.selectAll()
//        val links = listOf(VenueIdLink(1, 2), VenueIdLink(1, 3)) // {1,3} missing
        val circles = mutableSetOf<MutableSet<Int>>()
        links.forEach { (id1, id2) ->
            if (circles.none { circle ->
                    if (circle.contains(id1)) {
                        circle.add(id2)
                        true
                    } else if (circle.contains(id2)) {
                        circle.add(id1)
                        true
                    } else {
                        false
                    }
                }) {
                // no circle contained it, create a new one
                circles += mutableSetOf<Int>().also {
                    it.add(id1)
                    it.add(id2)
                }
            }
        }
//        circles.forEach {
//            println(it)
//        }

        val linksPerVenues = mutableMapOf<Int, MutableSet<Int>>()
        links.forEach { (id1, id2) ->
            linksPerVenues.getOrPut(id1) { mutableSetOf() } += id2
            linksPerVenues.getOrPut(id2) { mutableSetOf() } += id1
        }
        val newLinks = linksPerVenues.flatMap { (id, actual) ->
            val expected = circles.single { it.contains(id) } - id
            val missing = expected - actual
            missing.map { VenueIdLink(id, it) }
        }.toSet().distinct()

        newLinks.forEach {
            println(it)
            linkRepo.insert(it)
        }
    }
}
