package seepick.localsportsclub.persistence

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual

class VenueIdLinkTest : StringSpec({
    "equals" {
        VenueIdLink(1, 2) shouldBeEqual VenueIdLink(1, 2)
        VenueIdLink(1, 2) shouldBeEqual VenueIdLink(2, 1)
    }
    "in set" {
        val set = mutableSetOf<VenueIdLink>()
        set += VenueIdLink(1, 2)
        set += VenueIdLink(2, 1)
        set.shouldHaveSize(1)
    }
})
