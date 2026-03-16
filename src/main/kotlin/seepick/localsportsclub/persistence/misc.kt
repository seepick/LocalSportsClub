package seepick.localsportsclub.persistence

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.jdbc.select

fun IntIdTable.nextId(): Int =
    select(id)
        .orderBy(id, order = SortOrder.DESC)
        .limit(1).toList().firstOrNull()?.let { it[id].value + 1 } ?: 1
