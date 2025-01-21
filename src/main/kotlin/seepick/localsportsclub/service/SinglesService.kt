package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.SinglesDbo
import seepick.localsportsclub.persistence.SinglesRepo
import seepick.localsportsclub.service.model.Gcal
import seepick.localsportsclub.service.model.Plan
import seepick.localsportsclub.service.model.Preferences
import java.time.LocalDateTime

interface SinglesService {
    var notes: String
    var lastSync: LocalDateTime?
    var windowPref: WindowPref?
    var plan: Plan?
    var preferences: Preferences
}

class SinglesServiceImpl(
    private val singlesRepo: SinglesRepo,
) : SinglesService {
    private var cache: SinglesDbo? = null

    override var notes
        get() = cachedOrSelect().notes
        set(value) {
            update { copy(notes = value) }
        }
    override var lastSync: LocalDateTime?
        get() = cachedOrSelect().lastSync
        set(value) {
            update { copy(lastSync = value) }
        }

    override var windowPref: WindowPref?
        get() = cachedOrSelect().windowPref
        set(value) {
            update { copy(windowPref = value) }
        }

    override var plan: Plan?
        get() = cachedOrSelect().plan
        set(value) {
            update { copy(plan = value) }
        }

    override var preferences: Preferences
        get() = cachedOrSelect().preferences
        set(value) {
            update { copy(preferences = value) }
        }

    private fun update(withDbo: SinglesDbo.() -> SinglesDbo) {
        val dbo = cachedOrSelect()
        val updated = dbo.withDbo()
        singlesRepo.update(updated)
        cache = updated
    }

    private fun cachedOrSelect(): SinglesDbo =
        cache ?: run {
            val dbo = singlesRepo.select() ?: run {
                SinglesDbo(
                    notes = "",
                    lastSync = null,
                    plan = null,
                    windowPref = null,
                    preferences = Preferences(
                        uscCredentials = null,
                        periodFirstDay = null,
                        city = null,
                        home = null,
                        gcal = Gcal.GcalDisabled,
                    ),
                ).also {
                    singlesRepo.insert(it)
                }
            }
            dbo.also {
                cache = it
            }
        }

}

data class WindowPref(
    val width: Int,
    val height: Int,
    val posX: Int,
    val posY: Int,
) {
    fun toSqlString() = "$width,$height,$posX,$posY"

    companion object {
        val default = WindowPref(
            width = 1500,
            height = 1200,
            posX = 100,
            posY = 100,
        )

        fun readFromSqlString(string: String?): WindowPref? =
            string?.split(",")?.let {
                WindowPref(
                    width = it[0].toInt(),
                    height = it[1].toInt(),
                    posX = it[2].toInt(),
                    posY = it[3].toInt(),
                )
            }
    }
}
