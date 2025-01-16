package seepick.localsportsclub.tools

import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.api.venue.cleanVenueInfo
import seepick.localsportsclub.persistence.ActivityRepo
import seepick.localsportsclub.persistence.ExposedActivityRepo
import seepick.localsportsclub.persistence.ExposedFreetrainingRepo
import seepick.localsportsclub.persistence.ExposedVenueRepo
import seepick.localsportsclub.persistence.FreetrainingRepo
import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenueRepo
import seepick.localsportsclub.service.unescape

object DataCleaner {

    private const val prodMode = false

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
        cleanTexts()
        println("Done ✅")
    }

    private val activityRepo: ActivityRepo = ExposedActivityRepo
    private val freetrainingRepo: FreetrainingRepo = ExposedFreetrainingRepo
    private val venueRepo: VenueRepo = ExposedVenueRepo

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
}
