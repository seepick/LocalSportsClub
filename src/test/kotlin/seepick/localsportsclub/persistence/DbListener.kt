package seepick.localsportsclub.persistence

import io.kotest.core.listeners.AfterEachListener
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.DriverManager

class DbListener : BeforeEachListener, AfterEachListener {

    private lateinit var db: Database

    // see: https://github.com/JetBrains/Exposed/issues/726
    private lateinit var keepAliveConnection: Connection

    override suspend fun beforeEach(testCase: TestCase) {
        val jdbcUrl = testJdbcInmemoryUrl()
        db = Database.connect(jdbcUrl, setupConnection = ::enableSqliteForeignKeySupport)
        keepAliveConnection = DriverManager.getConnection(jdbcUrl)
        transaction(db) {
            SchemaUtils.create(*allTables)
        }
    }

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        TransactionManager.closeAndUnregister(db)
        keepAliveConnection.close()
    }
}

fun testJdbcInmemoryUrl(): String =
    "jdbc:sqlite:file:test${System.currentTimeMillis()}?mode=memory&cache=shared"
