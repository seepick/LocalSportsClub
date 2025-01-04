package seepick.localsportsclub.migration

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object MigrationSourceDecoder {
    private val json = Json {
        ignoreUnknownKeys = false
    }
    private val oldData = File(System.getProperty("user.home"), "Desktop/allfit_partners.json")
    fun decode(): List<MigrationPartner> {
        require(oldData.exists())
        return json.decodeFromString<MigrationJson>(oldData.readText()).partners.sortedBy { it.name }
    }
}

@Serializable
data class MigrationJson(
    val partners: List<MigrationPartner>
)

@Serializable
data class MigrationPartner(
    val id: Int,
    val name: String,
    val rating: Int,
    val note: String,
    val website: String?,
    val isWishlisted: Boolean,
    val isFavorited: Boolean,
    val isHidden: Boolean,
    val locations: List<MigrationLocation>,
    val checkins: List<MigrationCheckin> = emptyList(),
    val dropins: List<MigrationDropins> = emptyList(),
)

@Serializable
data class MigrationLocation(
    val streetName: String,
    val houseNumber: String,
    val addition: String,
    val zipCode: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class MigrationCheckin(
    val workoutName: String,
    val start: String, //ZonedDateTime,
    val end: String, //ZonedDateTime,
)

@Serializable
data class MigrationDropins(
    val createdAt: String, //ZonedDateTime,
)
