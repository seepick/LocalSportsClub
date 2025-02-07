package seepick.localsportsclub.service.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ActivityStateTest : StringSpec({
    "Names are unchanged as used as DB values" {
        ActivityState.entries.forEach {
            val sqlName = when (it) {
                ActivityState.Blank -> "Blank"
                ActivityState.Booked -> "Booked"
                ActivityState.Checkedin -> "Checkedin"
                ActivityState.Noshow -> "Noshow"
                ActivityState.CancelledLate -> "CancelledLate"
            }
            it.name shouldBe sqlName
        }
    }
})
