package seepick.localsportsclub.view.usage

import seepick.localsportsclub.service.WindowPref
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.service.model.Credentials
import seepick.localsportsclub.service.model.Plan
import seepick.localsportsclub.service.model.Preferences
import seepick.localsportsclub.service.singles.CityId
import seepick.localsportsclub.service.singles.SinglesService
import java.time.LocalDateTime

data class InMemorySinglesService(
    override var notes: String? = "",
    override var windowPref: WindowPref? = null,
    override var plan: Plan? = null,
    override var preferences: Preferences = Preferences.empty,
) : SinglesService {

    override var verifiedGcalId: String? = null
    override var verifiedUscCredentials: Credentials? = null
    val lastSyncs = mutableMapOf<CityId, LocalDateTime>()

    override fun getLastSyncFor(city: City): LocalDateTime? =
        lastSyncs[city.id]

    override fun setLastSyncFor(city: City, timestamp: LocalDateTime) {
        lastSyncs[city.id] = timestamp
    }
}
