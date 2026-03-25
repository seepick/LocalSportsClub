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

class ExposedTeacherRemarkRepoTest : DescribeSpec({
    val repo = ExposedTeacherRemarkRepo
    extension(DbListener())

    fun insertVenue(): VenueDbo = transaction {
        ExposedVenueRepo.insert(Arb.venueDbo().next())
    }

    fun insertRemark(withTeacher: TeacherRemarkDbo.() -> TeacherRemarkDbo = { this }): TeacherRemarkDbo = transaction {
        Arb.teacherRemarkDbo().next().let(withTeacher).also { dbo ->
            TeacherRemarkDboTable.insert {
                it[TeacherRemarkDboTable.id] = dbo.id
                it[TeacherRemarkDboTable.venueId] = dbo.venueId
                it[TeacherRemarkDboTable.name] = dbo.name
                it[TeacherRemarkDboTable.remark] = dbo.remark
                it[TeacherRemarkDboTable.rating] = dbo.rating
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
            val teacher = Arb.teacherRemarkDbo().next().copy(venueId = venue.id)

            repo.reset(venue.id, listOf(teacher))

            repo.selectAll().shouldBeSingleton().first() shouldBeEqual teacher.copy(id = 1)
        }
        it("Given empty When pass two Then both inserted") {
            val venue = insertVenue()

            repo.reset(
                venue.id, listOf(
                    Arb.teacherRemarkDbo().next().copy(venueId = venue.id),
                    Arb.teacherRemarkDbo().next().copy(venueId = venue.id),
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
                repo.reset(1, listOf(Arb.teacherRemarkDbo().next().copy(venueId = 2)))
            }
        }
    }
})

fun Arb.Companion.teacherRemarkDbo() = arbitrary {
    TeacherRemarkDbo(
        id = Arb.int().bind(),
        venueId = Arb.int().bind(),
        name = Arb.string().bind(),
        remark = Arb.string().bind(),
        rating = Arb.enum<RemarkDboRating>().bind(),
    )
}
