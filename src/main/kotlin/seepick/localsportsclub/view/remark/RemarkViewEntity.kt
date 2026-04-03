package seepick.localsportsclub.view.remark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.model.GlobalRemarkType
import seepick.localsportsclub.service.model.RemarkRating

class RemarkViewEntity(
    val id: Int,
    val type: RemarkViewType,
    name: String,
    remark: String,
    rating: RemarkRating,
) {
    var name = mutableStateOf(name)
    var remark by mutableStateOf(remark)
    var rating by mutableStateOf(rating)

    companion object {
        fun newPrototype(type: RemarkViewType) = RemarkViewEntity(
            id = -1,
            type = type,
            name = "",
            remark = "",
            rating = RemarkRating.default,
        )
    }
}

sealed interface RemarkViewType {
    data class WithVenue(val venueId: Int) : RemarkViewType
    data class ForGlobal(val type: GlobalRemarkType) : RemarkViewType
}
