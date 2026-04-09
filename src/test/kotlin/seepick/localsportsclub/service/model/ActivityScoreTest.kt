package seepick.localsportsclub.service.model

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.ranges.shouldBeIn
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import seepick.localsportsclub.view.remark.RemarkViewEntity
import seepick.localsportsclub.view.remark.RemarkViewType

class ActivityScoreTest : DescribeSpec({

    fun scoreResetActivity(code: ActivityBuilder.() -> Unit) = Activity.build {
        isFavorited = false
        isWishlisted = false
        state = ActivityState.Blank
        rating = Rating.R0
        category = Category("category", null)
        name = "test activity"
        teacher = null
        code()
    }

    fun nullableScoreOf(code: ActivityBuilder.() -> Unit) = scoreResetActivity(code).score
    fun scoreOf(code: ActivityBuilder.() -> Unit) = nullableScoreOf(code).shouldNotBeNull()

    describe("simple") {
        it("empty then null") {
            nullableScoreOf {
                // default is empty
            } shouldBe null
        }
        it("favorited alone") {
            scoreOf { isFavorited = true } shouldBeIn 0.7..0.9
        }
        it("wishlisted alone") {
            scoreOf { isWishlisted = true } shouldBeIn 0.55..0.7
        }
        it("favorited > wishlisted") {
            scoreOf { isFavorited = true } shouldBeGreaterThan scoreOf { isWishlisted = true }
        }
    }
    describe("k.o. red flags") {
        it("activity bad") {
            scoreOf {
                activityRemark(RemarkRating.Bad)
            } shouldBe 0.0
        }
        it("activity bad ignoring others") {
            scoreOf {
                activityRemark(RemarkRating.Bad)
                isFavorited = true
                rating = Rating.R5
            } shouldBe 0.0
        }
        it("teacher bad") {
            scoreOf {
                teacherRemark(RemarkRating.Bad)
            } shouldBe 0.0
        }
        it("teacher bad ignoring others") {
            scoreOf {
                teacherRemark(RemarkRating.Bad)
                isFavorited = true
                rating = Rating.R5
            } shouldBe 0.0
        }
    }
    describe("extremes") {
        it("max good") {
            scoreOf {
                isFavorited = true
                rating = Rating.R5
                activityRemark(RemarkRating.Amazing)
                teacherRemark(RemarkRating.Amazing)
            } shouldBe 1.0
        }
        it("semi-min good") {
            scoreOf {
                isFavorited = false
                rating = Rating.R1
                activityRemark(RemarkRating.Meh)
                teacherRemark(RemarkRating.Meh)
            } shouldBeIn 0.05..0.2
        }
        it("When score near middle Then nullify") {
            nullableScoreOf {
                isWishlisted = true
                rating = Rating.R2
            }.shouldBeNull()
        }
    }
    describe("rating") {
        it("R1") {
            scoreOf { rating = Rating.R1 } shouldBeIn 0.1..0.3
        }
        it("R2") {
            scoreOf { rating = Rating.R2 } shouldBeIn 0.2..0.4
        }
        it("R3") {
            scoreOf { rating = Rating.R3 } shouldBeIn 0.5..0.6
        }
        it("R4") {
            scoreOf { rating = Rating.R4 } shouldBeIn 0.6..0.8
        }
        it("R5") {
            scoreOf { rating = Rating.R5 } shouldBeIn 0.7..0.9
        }
        it("ratings in order") {
            scoreOf { rating = Rating.R1 } shouldBeLessThan scoreOf {
                rating = Rating.R2
            } shouldBeLessThan scoreOf { rating = Rating.R3 } shouldBeLessThan scoreOf {
                rating = Rating.R4
            } shouldBeLessThan scoreOf { rating = Rating.R5 }
        }
    }
    describe("comparative") {
        it("highscore with amazing teacher > highscore without") {
            scoreOf {
                isFavorited = true
                rating = Rating.R5
                teacherRemark(RemarkRating.Amazing)
            } shouldBeGreaterThan scoreOf {
                isFavorited = true
                rating = Rating.R5
            }
        }
        it("highscore with amazing teacher > highscore with good teacher") {
            scoreOf {
                isFavorited = true
                rating = Rating.R5
                teacherRemark(RemarkRating.Amazing)
            } shouldBeGreaterThan scoreOf {
                isFavorited = true
                rating = Rating.R5
                teacherRemark(RemarkRating.Good)
            }
        }
    }
})

fun Arb.Companion.activityRemark() = arbitrary {
    ActivityRemark(
        id = Arb.int().bind(),
        venueId = Arb.int().bind(),
        name = Arb.string().bind(),
        remark = Arb.string().bind(),
        rating = Arb.enum<RemarkRating>().bind(),
    )
}

fun Arb.Companion.teacherRemark() = arbitrary {
    TeacherRemark(
        id = Arb.int().bind(),
        venueId = Arb.int().bind(),
        name = Arb.string().bind(),
        remark = Arb.string().bind(),
        rating = Arb.enum<RemarkRating>().bind(),
    )
}

fun ActivityBuilder.activityRemark(rating: RemarkRating, name: String = "activity name with remark") {
    this.name = name
    activityRemarks += Arb.remarkViewEntity().next().also {
        it.name.value = name
        it.rating = rating
    }
}

fun ActivityBuilder.teacherRemark(rating: RemarkRating, name: String = "teacher name with remark") {
    this.teacher = name
    teacherRemarks += Arb.remarkViewEntity().next().also {
        it.name.value = name
        it.rating = rating
    }
}

fun Arb.Companion.remarkViewEntity() = arbitrary {
    RemarkViewEntity(
        id = int(1..1_000).bind(),
        name = string().bind(),
        type = RemarkViewType.WithVenue(42),
        remark = string().bind(),
        rating = enum<RemarkRating>().bind(),
    )
}
