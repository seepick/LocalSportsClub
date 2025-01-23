package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class DbListener : BeforeEachListener, AfterEachListener {

    private val log = KotlinLogging.logger {}
    private lateinit var db: Database

    override suspend fun beforeEach(testCase: TestCase) {
        db = Database.connect(buildTestJdbcUrl("LSC-test").also {
            log.debug { "Connecting to: [$it]" }
        }, setupConnection = ::enableSqliteForeignKeySupport)

        transaction {
//            exec("PRAGMA foreign_keys", transform = {
//                println("foreign keys are: [${it.getInt(1)}]")
//            })
            println("creating tables")
            SchemaUtils.create(*allTables)
        }
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        TransactionManager.closeAndUnregister(db)
    }
}

fun buildTestJdbcUrl(namePrefix: String): String =
    "jdbc:sqlite:${System.getProperty("java.io.tmpdir")}$namePrefix-${System.currentTimeMillis()}"
