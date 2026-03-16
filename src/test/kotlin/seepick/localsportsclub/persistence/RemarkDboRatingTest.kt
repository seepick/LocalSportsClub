package seepick.localsportsclub.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.equals.shouldBeEqual

class RemarkDboRatingTest : StringSpec({
    "names stable for SQL" {
        RemarkDboRating.entries.forEach {
            when (it) {
                RemarkDboRating.Amazing -> it.name shouldBeEqual "Amazing"
                RemarkDboRating.Good -> it.name shouldBeEqual "Good"
                RemarkDboRating.Meh -> it.name shouldBeEqual "Meh"
                RemarkDboRating.Bad -> it.name shouldBeEqual "Bad"
            }
        }
    }
})
