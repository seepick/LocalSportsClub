package seepick.localsportsclub.persistence.testInfra

import seepick.localsportsclub.persistence.VenueDbo
import seepick.localsportsclub.persistence.VenuesRepo

fun buildTestJdbcUrl(namePrefix: String): String =
    "jdbc:h2:mem:$namePrefix-${System.currentTimeMillis()};DB_CLOSE_DELAY=-1"

fun VenuesRepo.persist(vararg venue: VenueDbo) {
    persist(listOf(*venue))
}
