package seepick.localsportsclub.persistence

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
import seepick.localsportsclub.service.model.GlobalRemarkType

class GlobalRemarkExposedRepositoryTest : DescribeSpec({
    val repo = GlobalRemarkExposedRepository
    extension(DbListener())

    val remarkType = Arb.enum<GlobalRemarkType>().next()
    val remarkType1 = GlobalRemarkType.Activity
    val remarkType2 = GlobalRemarkType.Teacher

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
    describe("insertAll") {
        it("When insert it Then return it") {
            val remark = Arb.globalRemarkDbo().next().copy(id = 1)

            repo.insertAll(listOf(remark))

            repo.selectAll().shouldBeSingleton().first() shouldBeEqual remark
        }
    }
    // TODO delete this
//    describe("update") {
//        it("Given existing When update Then return updated") {
//            val remark = Arb.globalRemarkDbo().next().copy(id = 1)
//            repo.insert(listOf(remark))
//            val remarkUpdated = remark.copy(remark = remark.remark + " updated")
//
//            repo.update(listOf(remarkUpdated))
//
//            repo.selectAll().shouldBeSingleton().first() shouldBeEqual remarkUpdated
//        }
//    }
    describe("deleteAll") {
        it("Given existing When delete proper type Then return empty") {
            repo.insertAll(listOf(Arb.globalRemarkDbo().next().copy(type = remarkType)))

            repo.deleteAll(remarkType)

            repo.selectAll().shouldBeEmpty()
        }
        it("Given existing When delete other type Then still return it") {
            repo.insertAll(listOf(Arb.globalRemarkDbo().next().copy(type = remarkType1)))

            repo.deleteAll(remarkType2)

            repo.selectAll().shouldHaveSize(1)
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
