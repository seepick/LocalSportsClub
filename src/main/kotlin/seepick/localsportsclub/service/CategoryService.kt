package seepick.localsportsclub.service

import seepick.localsportsclub.service.model.Category
import seepick.localsportsclub.service.model.GlobalRemarkType

class CategoryService(
    private val globalRemarkService: GlobalRemarkService,
) {

    private val globalCategoryRemarks by lazy {
        globalRemarkService.selectAll(GlobalRemarkType.Category)
    }

    private val categoryCache = mutableMapOf<String, Category>()

    fun findCategoryByName(name: String): Category {
        return categoryCache.getOrPut(name) {
            Category(
                name = name,
                rating = globalCategoryRemarks.filter { remark ->
                    name.contains(remark.name.value, ignoreCase = true)
                }.let {
                    when (it.size) {
                        0 -> null
                        1 -> it.single().rating
                        else -> it.maxBy { it.name.value.length }.rating
                    }
                })
        }
    }
}
