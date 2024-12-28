package seepick.localsportsclub.view.venue.detail

import androidx.compose.runtime.mutableStateOf
import seepick.localsportsclub.service.model.Rating
import seepick.localsportsclub.service.model.Venue

class VenueEditModel {

    var notes = mutableStateOf("")
    var rating = mutableStateOf(Rating.R0)
    var isFavorited = mutableStateOf(false)
    var isWishlisted = mutableStateOf(false)
    var isHidden = mutableStateOf(false)
    var currentVenue: Venue? = null

    fun init(venue: Venue) {
        currentVenue = venue
        notes.value = venue.notes
        rating.value = venue.rating
        isFavorited.value = venue.isFavorited
        isWishlisted.value = venue.isWishlisted
        isHidden.value = venue.isHidden
    }

    fun updatePropertiesOf(selectedVenue: Venue) {
        selectedVenue.notes = notes.value
        selectedVenue.rating = rating.value
        selectedVenue.isFavorited = isFavorited.value
        selectedVenue.isWishlisted = isWishlisted.value
        selectedVenue.isHidden = isHidden.value
        selectedVenue.officialWebsite = selectedVenue.officialWebsite?.let { it.ifEmpty { null } }
    }

    fun isClean(): Boolean =
        currentVenue?.let { venue ->
            notes.value == venue.notes &&
                    rating.value == venue.rating &&
                    isFavorited.value == venue.isFavorited &&
                    isWishlisted.value == venue.isWishlisted &&
                    isHidden.value == venue.isHidden
        } ?: false
}
