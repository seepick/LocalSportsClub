package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.SinglesDbo
import seepick.localsportsclub.persistence.SinglesRepo
import java.time.LocalDateTime

class SinglesService(
    private val singlesRepo: SinglesRepo,
) {
    private var cache: SinglesDbo? = null

    fun readNotes() = cachedOrSelect().notes
    fun readLastSync() = cachedOrSelect().lastSync

    fun updateNotes(notes: String) {
        update { copy(notes = notes) }
    }

    fun updateLastSync(lastSync: LocalDateTime) {
        update { copy(lastSync = lastSync) }
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
                ).also {
                    singlesRepo.insert(it)
                }
            }
            dbo.also {
                cache = it
            }
        }
}
