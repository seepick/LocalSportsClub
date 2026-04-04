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
        "boxing sports", "boxing sports, muay thai", "modern self defense" -> "🥊"
        "cycling", "indoor cycling" -> "🚴🏻‍♀️"
        "dance", "dancing", "capoeira", "zumba" -> "💃🏻"
        "bootcamp", "fitness" -> "💪🏻"
        "climbing", "bouldering" -> "🧗🏻‍♀️"
        "football" -> "⚽"
        "massage" -> "💆🏻‍♀️"
        "tennis", "squash", "padel" -> "🎾"
        "stand up paddling" -> "🛶"
        "meditation" -> "🙏🏻"
        "ice skating" -> "⛸️"
        "gym", "crosstraining", "functional training", "hyrox", "personal training" -> "🏋️"
        "ems", "ems strength" -> "⚡"
        "family sports" -> "👨‍👩‍👧‍👦"
        "pilates", "pilates reformer", "barre" -> "🤸🏻‍♀️"
        "muay thai", "mixed martial arts", "jiu jitsu", "traditional asian martial arts" -> "🥋"
        "pole dance" -> "🪩"
        "relaxation" -> "🛀🏻"
        "running", "parkour", "hiking" -> "🏃🏻‍♀️"
        "trampoline" -> "🤾🏻‍♀️"
        "yoga", "hatha" -> "🧘🏻‍♀️"
        "qi gong and tai chi" -> "☯️"
        "wellness" -> "🧖🏻‍♀️"
        else -> null
    }

    val nameAndMaybeEmoji = name + if (emoji != null) " $emoji" else ""

    override fun compareTo(other: Category) = name.compareTo(other.name)
}
