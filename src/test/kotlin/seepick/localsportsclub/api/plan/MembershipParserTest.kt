package seepick.localsportsclub.api.plan

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import seepick.localsportsclub.readTestResponse
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Country
import seepick.localsportsclub.service.model.Plan

class MembershipParserTest : StringSpec() {
    init {
        "When parse Then return plan type" {
            MembershipParser.parse(readTestResponse<String>("activities.html")) shouldBe Membership(
                plan = Plan.Large,
                country = Country.byLabel("Netherlands"),
                city = City.byLabel("Amsterdam"),
            )
        }
    }
}
