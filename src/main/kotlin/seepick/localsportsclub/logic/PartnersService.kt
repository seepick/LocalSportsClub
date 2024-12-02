package seepick.localsportsclub.logic

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.persistence.PartnerDbo
import seepick.localsportsclub.persistence.PartnersRepo

interface PartnersService {
    fun insert()
    fun select()
}

class PartnersServiceImpl(
    private val partnersRepo: PartnersRepo
) : PartnersService {

    private val log = logger { }

    override fun insert() {
        log.info { "insert()" }
        transaction {
            val nextId = (partnersRepo.loadAll().maxOfOrNull { it.id } ?: 0) + 1
            partnersRepo.persist(PartnerDbo(id = nextId, name = "foo${nextId}"))
        }
    }

    override fun select() {
        log.info { "select()" }
        val partners = transaction {
            partnersRepo.loadAll()
        }
        log.debug { "Found ${partners.size} partners in DB." }
        partners.forEach {
            println(it)
        }
    }
}