package lsc.domain.model

// TODO restore interfaces?
enum class Rating(val value: Int) : Comparable<Rating> {
    R0(0),
    R1(1),
    R2(2),
    R3(3),
    R4(4),
    R5(5);

    companion object {
        private val ratingByValue by lazy {
            Rating.entries.associateBy { it.value }
        }

        fun byValue(rating: Int): Rating = ratingByValue[rating] ?: error("Invalid rating value: $rating")
    }

//    override val label = (0..<value).fold("") { acc, _ -> "${acc}${Lsc.icons.ratingEmoji}" }

    override fun toString(): String = "Rating$value"
}
