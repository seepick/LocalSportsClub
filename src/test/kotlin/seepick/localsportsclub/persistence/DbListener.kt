package seepick.localsportsclub.persistence

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class DbListener : BeforeEachListener, AfterEachListener {

    private lateinit var db: Database

    override suspend fun beforeEach(testCase: TestCase) {
        db = Database.connect(buildTestJdbcUrl("test"))
        transaction {
            SchemaUtils.create(*allTables)
            SchemaUtils.createSequence(ExposedVenueRepo.idSequence)
        }
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        TransactionManager.closeAndUnregister(db)
    }
}

fun buildTestJdbcUrl(namePrefix: String): String =
    "jdbc:h2:mem:$namePrefix-${System.currentTimeMillis()};DB_CLOSE_DELAY=-1"
