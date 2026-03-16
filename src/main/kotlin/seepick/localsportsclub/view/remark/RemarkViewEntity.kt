package seepick.localsportsclub.view.remark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import seepick.localsportsclub.service.model.RemarkRating

class RemarkViewEntity(
    val id: Int,
    val venueId: Int,
    name: String,
    remark: String,
    rating: RemarkRating,
) {
    var name by mutableStateOf(name)
    var remark by mutableStateOf(remark)
    var rating by mutableStateOf(rating)
}
