package seepick.localsportsclub.tools

import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.ExposedActivityRepo
import seepick.localsportsclub.persistence.ExposedFreetrainingRepo
import seepick.localsportsclub.persistence.ExposedVenueRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueRepo

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

//        cleanActivityNames()
//        cleanVenueAddresses()
        println("Done ✅")
    }

    private val activityRepo: ActivityRepo = ExposedActivityRepo
    private val freetrainingRepo: FreetrainingRepo = ExposedFreetrainingRepo
    private val venueRepo: VenueRepo = ExposedVenueRepo

    private fun cleanActivityNames() = transaction {
        activityRepo.selectAll().filter {
            it.name.startsWith(" ") || it.name.endsWith(" ")
        }.also { println("Fixing ${it.size} activities") }.forEach {
            activityRepo.update(it.copy(name = it.name.trim()))
        }
        freetrainingRepo.selectAll().filter {
            it.name.startsWith(" ") || it.name.endsWith(" ")
        }.also { println("Fixing ${it.size} freetrainings") }.forEach {
            freetrainingRepo.update(it.copy(name = it.name.trim()))
        }
    }

    private fun cleanVenueAddresses() = transaction {
        venueRepo.selectBySlug("vondelpark-rosepark-rosarium")!!.also { venue ->
            venueRepo.update(
                venue.copy(
                    postalCode = "1071 AL",
                    notes = "Coordinates: 52°21'27.6\"N 4°51'46.7\"E"
                )
            )
        }
        venueRepo.selectBySlug("fitness-academy-amsterdam-olvg")!!.also { venue ->
            venueRepo.update(venue.copy(street = "'s-Gravesandeplein"))
        }
        venueRepo.selectBySlug("studio_balance")!!.also { venue ->
            venueRepo.update(
                venue.copy(
                    street = "Rijnstraat 63", postalCode = "1079 GW", addressLocality = "Amsterdam, Netherlands",
                    latitude = "52.3459377", longitude = "4.9056754"
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
}
