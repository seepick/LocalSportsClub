package seepick.localsportsclub.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import seepick.localsportsclub.persistence.activityDbo

class NameFixingActivityDboProcessorTest : StringSpec({

    listOf(
        "okay" to "okay",
        " whitespaceBefore" to "whitespaceBefore",
        "whitespaceAfter " to "whitespaceAfter",
        " whitespaceBoth " to "whitespaceBoth",
        ".dotBefore " to "dotBefore",
        "dotAfter. " to "dotAfter",
        ".dotBoth. " to "dotBoth",
        "_underlineBefore " to "underlineBefore",
        "underlineAfter_ " to "underlineAfter",
        "_underlineBoth_ " to "underlineBoth",
        "*starBefore " to "starBefore",
        "starAfter* " to "starAfter",
        "*starBoth* " to "starBoth",
        "_*.manyBefore " to "manyBefore",
        "_*.manyAfter" to "manyAfter",
        "_*.manyBoth *_. " to "manyBoth",
        "keepPlus+" to "keepPlus+",
    ).forEach { (name, expected) ->
        "Given [$name] should be [$expected]" {
            NameFixingActivityDboProcessor().process(Arb.activityDbo().next().copy(name = name)).name shouldBe expected
        }
    }
})
