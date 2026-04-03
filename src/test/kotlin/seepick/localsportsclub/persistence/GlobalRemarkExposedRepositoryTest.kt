package seepick.localsportsclub.persistence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class GlobalRemarkExposedRepositoryTest : DescribeSpec({
    val repo = GlobalRemarkExposedRepository
    extension(DbListener())

    fun insertRemark(): GlobalRemarkDbo = transaction {
        val dbo = Arb.globalRemarkDbo().next().copy(id = 1)
        GlobalRemarkDboTable.insert {
            it[GlobalRemarkDboTable.id] = dbo.id
            it[GlobalRemarkDboTable.type] = dbo.type
            it[GlobalRemarkDboTable.name] = dbo.name
            it[GlobalRemarkDboTable.rating] = dbo.rating
            it[GlobalRemarkDboTable.remark] = dbo.remark
        }
        dbo
    }

    describe("selectAll") {
        it("Given empty Then return empty") {
            repo.selectAll().shouldBeEmpty()
        }
        it("Given venue with teacher Then return it") {
            val remark = insertRemark()

            repo.selectAll().shouldBeSingleton().first() shouldBeEqual remark
        }
    }
    describe("insert") {
        it("When insert it Then return it") {
            val remark = Arb.globalRemarkDbo().next().copy(id = 1)

            repo.insert(listOf(remark))

            repo.selectAll().shouldBeSingleton().first() shouldBeEqual remark
        }
    }
    describe("update") {
        it("Given existing When update Then return updated") {
            val remark = Arb.globalRemarkDbo().next().copy(id = 1)
            repo.insert(listOf(remark))
            val remarkUpdated = remark.copy(remark = remark.remark + " updated")

            repo.update(listOf(remarkUpdated))

            repo.selectAll().shouldBeSingleton().first() shouldBeEqual remarkUpdated
        }
    }
    describe("delete") {
        it("Given existing When delete Then return empty") {
            val remark = Arb.globalRemarkDbo().next()
            val inserted = repo.insert(listOf(remark)).first()

            repo.delete(listOf(inserted))

            repo.selectAll().shouldBeEmpty()
        }
    }
})

fun Arb.Companion.globalRemarkDbo() = arbitrary {
    GlobalRemarkDbo(
        id = int().bind(),
        type = enum<GlobalRemarkType>().bind(),
        name = string(1..128).bind(),
        remark = string(1..256).bind(),
        rating = enum<RemarkDboRating>().bind(),
    )
}
