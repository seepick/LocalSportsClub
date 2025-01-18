package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.SinglesDbo
import seepick.localsportsclub.persistence.SinglesRepo
import seepick.localsportsclub.service.model.Gcal
import seepick.localsportsclub.service.model.Preferences
import java.time.LocalDateTime

class SinglesService(
    private val singlesRepo: SinglesRepo,
) {
    private var cache: SinglesDbo? = null

    fun readNotes() = cachedOrSelect().notes
    fun updateNotes(notes: String) {
        update { copy(notes = notes) }
    }

    fun readLastSync() = cachedOrSelect().lastSync
    fun updateLastSync(lastSync: LocalDateTime) {
        update { copy(lastSync = lastSync) }
    }

    fun readWindowPref() = cachedOrSelect().windowPref
    fun updateWindowPref(windowPref: WindowPref) {
        update { copy(windowPref = windowPref) }
    }

    fun readPreferences() = cachedOrSelect().preferences
    fun updatePreferences(preferences: Preferences) {
        update { copy(preferences = preferences) }
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
                    windowPref = null,
                    preferences = Preferences(
                        uscCredentials = null,
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
