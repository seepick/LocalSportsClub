package seepick.localsportsclub.service.model

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Category(
    val name: String,
    val rating: RemarkRating?,
) : Comparable<Category> {

    var activityCount by mutableStateOf(0)
    var freetrainingCount by mutableStateOf(0)
    var venueCount by mutableStateOf(0)
    val nameAndEmojiAndActivityCount by derivedStateOf { "$nameAndMaybeEmoji ($activityCount)" }
    val nameAndEmojiAndFreetrainingCount by derivedStateOf { "$nameAndMaybeEmoji ($freetrainingCount)" }
    val nameAndEmojiAndVenueCount by derivedStateOf { "$nameAndMaybeEmoji ($venueCount)" }

    val emoji: String? = when (name.lowercase()) {
        "aerial" -> "🪂"
        "aqua", "swimming" -> "🏊🏻‍♀️"
        "bootcamp", "fitness" -> "💪🏻"
        "boxing sports", "boxing sports, muay thai", "modern self defense" -> "🥊"
        "climbing", "bouldering" -> "🧗🏻‍♀️"
        "cycling", "indoor cycling" -> "🚴🏻‍♀️"
        "dance", "dancing", "capoeira", "zumba" -> "💃🏻"
        "ems", "ems strength" -> "⚡"
        "family sports" -> "👨‍👩‍👧‍👦"
        "football" -> "⚽"
        "gym", "crosstraining", "functional training", "hyrox", "personal training" -> "🏋️"
        "ice skating" -> "⛸️"
        "massage" -> "💆🏻‍♀️"
        "meditation" -> "🙏🏻"
        "muay thai", "mixed martial arts", "jiu jitsu", "traditional asian martial arts" -> "🥋"
        "pilates", "pilates reformer", "barre" -> "🤸🏻‍♀️"
        "pole dance" -> "🪩"
        "qi gong and tai chi" -> "☯️"
        "relaxation" -> "🛀🏻"
        "running", "parkour", "hiking" -> "🏃🏻‍♀️"
        "stand up paddling" -> "🛶"
        "tennis", "squash", "padel" -> "🎾"
        "trampoline" -> "☄️"
        "wellness", "sauna" -> "🧖🏻‍♀️"
        "surfing" -> "🏄🏻‍♂️"
        "yoga", "hatha" -> "🧘🏻‍♀️"
        else -> null
    }

    val nameAndMaybeEmoji = name + if (emoji != null) " $emoji" else ""

    override fun compareTo(other: Category) = name.compareTo(other.name)
}
