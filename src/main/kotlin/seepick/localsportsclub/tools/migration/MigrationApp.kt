package seepick.localsportsclub.tools.migration

import org.jetbrains.exposed.sql.transactions.transaction
import seepick.localsportsclub.persistence.ExposedVenueRepo
import seepick.localsportsclub.service.model.City
import seepick.localsportsclub.tools.cliConnectToDatabase
import java.io.File

object MigrationApp {

    private val onefitExportFile = File(System.getProperty("user.home"), "Desktop/allfit_partners.json")

    @JvmStatic
    fun main(args: Array<String>) {
        cliConnectToDatabase(isProd = false)
        val venues = ExposedVenueRepo.selectAllByCity(City.Amsterdam.id).sortedBy { it.name }
        transaction {
            val onefitPartners = OnefitPartners.decode(onefitExportFile)
            val matches = MigrationMatcher.match(onefitPartners, venues)
            MigrationProcessor.process(matches, venues)
        }
        println("Done ✅")
    }
}
