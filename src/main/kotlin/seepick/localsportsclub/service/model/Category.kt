package seepick.localsportsclub.service.model

data class Category(
    val name: String,
) : Comparable<Category> {
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
