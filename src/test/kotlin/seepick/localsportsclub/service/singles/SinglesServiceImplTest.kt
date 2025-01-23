package seepick.localsportsclub.service.singles

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import seepick.localsportsclub.persistence.InMemorySinglesRepo
import seepick.localsportsclub.persistence.SinglesDbo
import seepick.localsportsclub.persistence.credentials
import seepick.localsportsclub.persistence.location
import seepick.localsportsclub.persistence.preferences
import seepick.localsportsclub.service.Encrypter
import seepick.localsportsclub.service.model.City

class SinglesServiceImplTest : StringSpec() {

    private val notes = Arb.string().next()
    private val prefs = Arb.preferences().next()

    private lateinit var singlesRepo: InMemorySinglesRepo
    private lateinit var singlesService: SinglesServiceImpl

    override suspend fun beforeEach(testCase: TestCase) {
        singlesRepo = InMemorySinglesRepo()
        singlesService = SinglesServiceImpl(singlesRepo)
    }

    init {
        "When get something Then empty singles v1 stored" {
            singlesService.notes

            singlesRepo.stored shouldBe SinglesDbo(1, Json.encodeToString(SinglesVersionCurrent.empty))
        }
        "When set notes Then stored in repo" {
            singlesService.notes = notes

            singlesRepo.readSingles().notes shouldBe notes
        }
        "When set preferences Then stored encrypted in repo" {
            val home = Arb.location().next()
            val credentials = Arb.credentials().next()
            val prefs = prefs.copy(home = home, city = City.Amsterdam, uscCredentials = credentials)
            singlesService.preferences = prefs

            singlesRepo.readSingles().also {
                it.prefHomeLong shouldBe home.longitude
                it.prefHomeLat shouldBe home.latitude
                it.prefCityId shouldBe prefs.city?.id
                it.prefUscCredUsername shouldBe credentials.username
                it.prefUscCredPassword shouldBe Encrypter.encrypt(credentials.password)
                it.prefPeriodFirstDay shouldBe prefs.periodFirstDay
                it.prefGoogleCalendarId shouldBe prefs.gcal.maybeCalendarId
            }
            singlesService.preferences.uscCredentials.shouldNotBeNull().password shouldBe credentials.password
        }
    }

    private fun InMemorySinglesRepo.readSingles(): SinglesVersionCurrent =
        stored.shouldNotBeNull().json.let { Json.decodeFromString(it) }
}
