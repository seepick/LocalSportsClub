package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.persistence.GlobalRemarkDbo
import seepick.localsportsclub.persistence.GlobalRemarkRepository
import seepick.localsportsclub.service.model.GlobalRemarkType
import seepick.localsportsclub.service.model.toRemarkDboRating
import seepick.localsportsclub.service.model.toRemarkRating
import seepick.localsportsclub.view.remark.RemarkViewEntity
import seepick.localsportsclub.view.remark.RemarkViewType

class GlobalRemarkService(
    private val repo: GlobalRemarkRepository,
) {
    private val log = logger {}

    fun selectAll(type: GlobalRemarkType): List<RemarkViewEntity> {
        return repo.selectAll().filter { it.type == type }.map {
            RemarkViewEntity(
                id = it.id,
                type = RemarkViewType.ForGlobal(type),
                name = it.name,
                remark = it.remark,
                rating = it.rating.toRemarkRating(),
            )
        }
    }

    fun reset(type: GlobalRemarkType, remarks: List<RemarkViewEntity>) {
        log.debug { "reset(type=$type, remarks.size=${remarks.size})" }
        repo.deleteAll(type)
        repo.insertAll(remarks.map { it.toGlobalRemarkDbo() })
    }
}

fun RemarkViewEntity.toGlobalRemarkDbo(): GlobalRemarkDbo {
    require(type is RemarkViewType.ForGlobal)
    return GlobalRemarkDbo(
        id = id,
        type = type.type,
        name = name.value,
        remark = remark,
        rating = rating.toRemarkDboRating(),
    )
}
