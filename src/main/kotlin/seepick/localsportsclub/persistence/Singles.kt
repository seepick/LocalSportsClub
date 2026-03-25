package seepick.localsportsclub.persistence

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

object SingleDboTable : Table("SINGLES") {
    val version = integer("VERSION")
    val json = text("JSON")
}

data class SinglesDbo(
    val version: Int,
    val json: String,
) {
    override fun toString() =
        "SinglesDbo[version=$version, json=...]"
}

interface SinglesRepo {
    fun select(): SinglesDbo?
    fun insert(singles: SinglesDbo)
    fun update(singles: SinglesDbo)
}

object ExposedSinglesRepo : SinglesRepo {

    private val log = logger {}

    override fun select() = transaction {
        log.debug { "select()" }
        SingleDboTable.selectAll().singleOrNull()?.let { row ->
            SinglesDbo(
                version = row[SingleDboTable.version],
                json = row[SingleDboTable.json],
            )
        }
    }

    override fun insert(singles: SinglesDbo): Unit = transaction {
        log.debug { "insert($singles)" }
        require(SingleDboTable.selectAll().toList().isEmpty())
        SingleDboTable.insert {
            it[version] = singles.version
            it[json] = singles.json
        }
    }

    override fun update(singles: SinglesDbo): Unit = transaction {
        log.debug { "update($singles)" }
        require(SingleDboTable.selectAll().toList().size == 1)
        SingleDboTable.update {
            it[version] = singles.version
            it[json] = singles.json
        }
    }

    fun reset(): Unit = transaction {
        SingleDboTable.deleteAll()
    }
}
