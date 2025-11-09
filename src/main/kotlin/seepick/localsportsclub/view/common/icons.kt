package seepick.localsportsclub.view.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

@Suppress("ConstPropertyName")
object LscIcons {
    private fun loadIcon(filename: String): ImageBitmap = readImageBitmapFromClasspath("/icons/$filename")
    val favoritedFull: ImageBitmap = loadIcon("favorite_full.png")
    val favoritedOutline = loadIcon("favorite_outline.png")
    val favorited2 = favoritedFull to favoritedOutline
    val favoritedIndicator = VisualIndicator.BitmapIndicator(favoritedFull)

    val wishlistedFull = loadIcon("wishlist_full.png")
    val wishlistedOutline = loadIcon("wishlist_outline.png")
    val wishlisted2 = wishlistedFull to wishlistedOutline
    val wishlistedIndicator = VisualIndicator.BitmapIndicator(wishlistedFull)

    val dateIndicator = VisualIndicator.EmojiIndicator("📆")
    const val checkedinEmoji = "📝"
    val checkedinIndicator = VisualIndicator.EmojiIndicator(checkedinEmoji)
    const val reservedEmoji = "👟" // booked (activity) or scheduled (freetraining)
    val reservedEmojiIndicator = VisualIndicator.EmojiIndicator(reservedEmoji)
    const val distanceEmoji = "🚌"
    val distanceEmojiIndicator = VisualIndicator.EmojiIndicator(distanceEmoji)
    const val noshowEmoji = "🚷"
    val noshowEmojiIndicator = VisualIndicator.EmojiIndicator(noshowEmoji)
    const val cancelledLateEmoji = "🚯"
    const val hiddenEmoji = "🙈"
    val hiddenIndicator = VisualIndicator.EmojiIndicator(hiddenEmoji)
    const val ratingEmoji = "⭐️"
    val ratingIndicator = VisualIndicator.EmojiIndicator(ratingEmoji)
    val categoryIndicator = VisualIndicator.EmojiIndicator("🗂️")
    val activities: ImageVector = Icons.Default.SportsGymnastics
    val activitiesIndicator = VisualIndicator.VectorIndicator(activities)
    val freetrainings = Icons.Default.FitnessCenter
    val freetrainingsIndicator = VisualIndicator.VectorIndicator(freetrainings)
    val venues = Icons.Default.HolidayVillage
    val notes = Icons.Default.Description
    val preferences = Icons.Default.Settings
}

@Suppress("UnusedReceiverParameter")
val Icons.Lsc get() = LscIcons
