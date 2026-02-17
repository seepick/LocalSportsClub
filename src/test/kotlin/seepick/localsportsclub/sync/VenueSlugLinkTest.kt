package seepick.localsportsclub.sync

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import seepick.localsportsclub.sync.domain.VenueSlugLink

class VenueSlugLinkTest : StringSpec({
    "equals" {
        VenueSlugLink("a", "b") shouldBeEqual VenueSlugLink("a", "b")
        VenueSlugLink("a", "b") shouldBeEqual VenueSlugLink("b", "a")
    }
    "in set" {
        val set = mutableSetOf<VenueSlugLink>()
        set += VenueSlugLink("a", "b")
        set += VenueSlugLink("b", "a")
        set.shouldHaveSize(1)
    }
})
