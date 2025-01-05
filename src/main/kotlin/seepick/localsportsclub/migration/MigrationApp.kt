package seepick.localsportsclub.migration

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.persistence.ExposedVenueRepo
import java.io.File

object MigrationApp {

    private val onefitExportFile = File(System.getProperty("user.home"), "Desktop/allfit_partners.json")

    @JvmStatic
    fun main(args: Array<String>) {
        connectToDatabase()
        val venues = ExposedVenueRepo.selectAll().sortedBy { it.name }
        transaction {
            val onefitPartners = OnefitPartners.decode(onefitExportFile)
            val matches = MigrationMatcher.match(onefitPartners, venues)
            MigrationProcessor.process(matches, venues)
        }
        println("Done ✅")
    }

    private fun connectToDatabase() {
        val home = File(System.getProperty("user.home"))
        val connect = ".lsc"
//        val connect = ".lsc-dev"
        val dbDir = File(home, "$connect/database")
        val jdbcUrl = "jdbc:h2:file:${dbDir.absolutePath}/h2"
        println("Connecting to database for migration: $jdbcUrl")
        Database.connect(jdbcUrl)
    }
}
