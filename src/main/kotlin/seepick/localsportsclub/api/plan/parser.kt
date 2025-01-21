package seepick.localsportsclub.api.plan

import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import seepick.localsportsclub.serializerLenient
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Country
import seepick.localsportsclub.service.model.Plan

object MembershipParser {
    fun parse(string: String): Membership {
        val json = Jsoup.parse(string).select("select#city_id").attr("data-datalayer")
        val member = serializerLenient.decodeFromString<MemberJson>(json)
        return Membership(
            plan = Plan.byApiString(member.user.membership_plan),
            city = City.byLabel(member.city),
            country = Country.byLabel(member.country),
        )
    }
}

@Serializable
data class MemberJson(
    val event: String,
    val city: String,
    val country: String,
    val user: MemberUserJson,
)

@Serializable
data class MemberUserJson(
    val id: String,
    val login_status: String, // logged-in
    val membership_city: String,
    val membership_country: String,
    val membership_status: String, // active
    val membership_plan: String, // L
    val membership_b2b_type: String, // b2c
    val membership_contract_duration: String, // monthly
    val company_name: String?,
)
