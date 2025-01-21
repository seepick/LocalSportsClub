package seepick.localsportsclub.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ExposedVenueLinksRepoTest : DescribeSpec() {

    private val cityId = 20
    private val cityId1 = 21
    private val cityId2 = 22

    init {
        extension(DbListener())
        describe("selectAll") {
            it("Given link When city matches Then return it") {
                val (venueId1, venueId2) = insertVenuesForCities(cityId, cityId)
                ExposedVenueLinksRepo.insert(VenueIdLink(venueId1, venueId2))

                ExposedVenueLinksRepo.selectAll(cityId).shouldBeSingleton()
            }
            it("Given link When city mismatches Then return empty") {
                val (venueId1, venueId2) = insertVenuesForCities(cityId1, cityId1)
                ExposedVenueLinksRepo.insert(VenueIdLink(venueId1, venueId2))

                ExposedVenueLinksRepo.selectAll(cityId2).shouldBeEmpty()
            }
            it("Given link When city for venue1 mismatches Then return empty") {
                val (venueId1, venueId2) = insertVenuesForCities(cityId1, cityId2)
                ExposedVenueLinksRepo.insert(VenueIdLink(venueId1, venueId2))

                ExposedVenueLinksRepo.selectAll(cityId2).shouldBeEmpty()
            }
            it("Given link When city for venue2 mismatches Then return empty") {
                val (venueId1, venueId2) = insertVenuesForCities(cityId2, cityId1)
                ExposedVenueLinksRepo.insert(VenueIdLink(venueId1, venueId2))

                ExposedVenueLinksRepo.selectAll(cityId2).shouldBeEmpty()
            }
        }
    }

    private fun insertVenuesForCities(cityId1: Int, cityId2: Int): Pair<Int, Int> {
        val venueId1 = ExposedVenueRepo.insert(Arb.venueDbo().next().copy(cityId = cityId1)).id
        val venueId2 = ExposedVenueRepo.insert(Arb.venueDbo().next().copy(cityId = cityId2)).id
        return venueId1 to venueId2
    }
}
