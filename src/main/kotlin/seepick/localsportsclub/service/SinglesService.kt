package seepick.localsportsclub.service

import seepick.localsportsclub.persistence.SinglesDbo
import seepick.localsportsclub.persistence.SinglesRepo

class SinglesService(
    private val singlesRepo: SinglesRepo,
) {
    private var cache: SinglesDbo? = null

    fun readNotes() = cacheOrLoad().notes

    fun updateNotes(notes: String) {
        update { copy(notes = notes) }
    }

    private fun update(withDbo: SinglesDbo.() -> SinglesDbo) {
        val dbo = cacheOrLoad()
        singlesRepo.update(dbo.withDbo())
        cache = dbo
    }

    private fun cacheOrLoad(): SinglesDbo =
        cache ?: run {
            val dbo = singlesRepo.select() ?: run {
                SinglesDbo(notes = "").also {
                    singlesRepo.insert(it)
                }
            }
            dbo.also {
                cache = it
            }
        }
}
