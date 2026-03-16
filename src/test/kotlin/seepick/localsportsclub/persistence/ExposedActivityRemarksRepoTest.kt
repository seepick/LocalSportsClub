package seepick.localsportsclub.persistence

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ExposedActivityRemarksRepoTest : DescribeSpec({
    val repo = ExposedActivityRemarkRepo
    extension(DbListener())

    fun insertVenue(): VenueDbo = transaction {
        ExposedVenueRepo.insert(Arb.venueDbo().next())
    }

    fun insertRemark(withActivity: ActivityRemarkDbo.() -> ActivityRemarkDbo = { this }): ActivityRemarkDbo =
        transaction {
            Arb.activityRemarkDbo().next().let(withActivity).also { dbo ->
                ActivityRemarksTable.insert {
                    it[ActivityRemarksTable.id] = dbo.id
                    it[ActivityRemarksTable.venueId] = dbo.venueId
                    it[ActivityRemarksTable.name] = dbo.name
                    it[ActivityRemarksTable.remark] = dbo.remark
                    it[ActivityRemarksTable.rating] = dbo.rating
                }
            }
        }

    describe("selectAll") {
        it("Given empty Then return empty") {
            repo.selectAll().shouldBeEmpty()
        }
        it("Given venue with teacher Then return it") {
            val venue = insertVenue()
            val teacher = insertRemark { copy(venueId = venue.id) }

            repo.selectAll().shouldBeSingleton().first() shouldBeEqual teacher
        }
    }

    describe("reset") {
        it("Given empty When pass empty Then nothing changed") {
            repo.reset(0, emptyList())

            repo.selectAll().shouldBeEmpty()
        }
        it("Given empty When pass single Then new inserted") {
            val venue = insertVenue()
            val activity = Arb.activityRemarkDbo().next().copy(venueId = venue.id)

            repo.reset(venue.id, listOf(activity))

            repo.selectAll().shouldBeSingleton().first() shouldBeEqual activity.copy(id = 1)
        }
        it("Given empty When pass two Then both inserted") {
            val venue = insertVenue()

            repo.reset(
                venue.id, listOf(
                    Arb.activityRemarkDbo().next().copy(venueId = venue.id),
                    Arb.activityRemarkDbo().next().copy(venueId = venue.id),
                )
            )

            repo.selectAll() shouldHaveSize 2
        }
        it("Given existing When reset venue without new teachers Then old deleted") {
            val venue = insertVenue()
            insertRemark { copy(venueId = venue.id) }

            repo.reset(venue.id, emptyList())

            repo.selectAll().shouldBeEmpty()
        }
        it("Given existing When reset for different venue Then untouched") {
            val venue = insertVenue()
            insertRemark { copy(venueId = venue.id) }

            repo.reset(venue.id + 1, emptyList())

            repo.selectAll().shouldBeSingleton()
        }
        it("When pass two different venues Then fail") {
            shouldThrow<IllegalArgumentException> {
                repo.reset(1, listOf(Arb.activityRemarkDbo().next().copy(venueId = 2)))
            }
        }
    }
})

fun Arb.Companion.activityRemarkDbo() = arbitrary {
    ActivityRemarkDbo(
        id = Arb.int().bind(),
        venueId = Arb.int().bind(),
        name = Arb.string().bind(),
        remark = Arb.string().bind(),
        rating = Arb.enum<RemarkDboRating>().bind(),
    )
}
