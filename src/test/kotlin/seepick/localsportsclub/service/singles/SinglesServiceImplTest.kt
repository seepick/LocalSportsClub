package seepick.localsportsclub.service.singles

import com.github.seepick.uscclient.model.City
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.serialization.json.Json
import seepick.localsportsclub.persistence.InMemorySinglesRepo
import seepick.localsportsclub.persistence.SinglesDbo
import seepick.localsportsclub.persistence.location
import seepick.localsportsclub.persistence.preferences
import seepick.localsportsclub.service.Encrypter
import testfixtUsc.credentials
import java.time.LocalDateTime

class SinglesServiceImplTest : StringSpec() {

    private val notes = Arb.string().next()
    private val prefs = Arb.preferences().next()
    private val timestamp = LocalDateTime.now()

    private lateinit var singlesRepo: InMemorySinglesRepo
    private lateinit var singlesService: SinglesServiceImpl

    override suspend fun beforeEach(testCase: TestCase) {
        singlesRepo = InMemorySinglesRepo()
        singlesService = SinglesServiceImpl(singlesRepo)
    }

    init {
        "When get something Then empty singles current version stored" {
            singlesService.notes

            singlesRepo.stored shouldBe SinglesDbo(
                SinglesVersionCurrent.VERSION,
                Json.encodeToString(SinglesVersionCurrent.empty)
            )
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
                it.prefUscCredentials shouldBe JsonCredentials(
                    username = credentials.username,
                    encryptedPassword = Encrypter.encrypt(credentials.password)
                )
                it.prefPeriodFirstDay shouldBe prefs.periodFirstDay
                it.prefGoogleCalendarId shouldBe prefs.gcal.maybeCalendarId
            }
            singlesService.preferences.uscCredentials.shouldNotBeNull().password shouldBe credentials.password
        }
        "When get last sync Then return it" {
            singlesService.getLastSyncFor(City.Amsterdam).shouldBeNull()
        }
        "When set last sync Then store it" {
            singlesService.setLastSyncFor(City.Amsterdam, timestamp)
            singlesService.getLastSyncFor(City.Amsterdam) shouldBe timestamp
            singlesService.getLastSyncFor(City.Berlin).shouldBeNull()
        }
    }

    private fun InMemorySinglesRepo.readSingles(): SinglesVersionCurrent =
        stored.shouldNotBeNull().json.let { Json.decodeFromString(it) }
}
