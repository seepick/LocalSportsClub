package seepick.localsportsclub.tools.migration

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object OnefitPartners {

    private val json = Json {
        ignoreUnknownKeys = false
    }

    fun decode(oldData: File): List<OnefitPartner> {
        require(oldData.exists())
        return json.decodeFromString<OnefitPartnersJson>(oldData.readText()).partners.sortedBy { it.name }
    }
}

@Serializable
data class OnefitPartnersJson(
    val partners: List<OnefitPartner>
)

@Serializable
data class OnefitPartner(
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
