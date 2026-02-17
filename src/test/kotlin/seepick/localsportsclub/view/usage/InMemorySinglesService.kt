package seepick.localsportsclub.view.usage

import com.github.seepick.uscclient.login.Credentials
import com.github.seepick.uscclient.model.City
import com.github.seepick.uscclient.plan.Plan
import seepick.localsportsclub.service.WindowPref
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
