package seepick.localsportsclub.view.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.ui.graphics.vector.ImageVector

@Suppress("ConstPropertyName")
object LscIcons {

    const val favoritedEmoji = "❤️"
    const val wishlistedEmoji = "💡"
    val favoritedIndicator = VisualIndicator.EmojiIndicator(favoritedEmoji)
    val wishlistedIndicator = VisualIndicator.EmojiIndicator(wishlistedEmoji)

    val dateIndicator = VisualIndicator.EmojiIndicator("📆")
    const val checkedinEmoji = "📝"
    val checkedinIndicator = VisualIndicator.EmojiIndicator(checkedinEmoji)
    const val reservedEmoji = "👟" // booked (activity) or scheduled (freetraining)
    val reservedIndicator = VisualIndicator.EmojiIndicator(reservedEmoji)
    const val distanceEmoji = "🚌"
    val distanceIndicator = VisualIndicator.EmojiIndicator(distanceEmoji)
    const val noshowEmoji = "🚷"
    const val cancelledLateEmoji = "🚯"
    const val hiddenEmoji = "🙈"
    val hiddenIndicator = VisualIndicator.EmojiIndicator(hiddenEmoji)
    const val ratingEmoji = "⭐️"
    val ratingIndicator = VisualIndicator.EmojiIndicator(ratingEmoji)
    val categoryIndicator = VisualIndicator.EmojiIndicator("🗂️")
    val activities: ImageVector = Icons.Default.SportsGymnastics
    val teachers: ImageVector = Icons.Default.Face
    val teachersIndicator = VisualIndicator.VectorIndicator(teachers)
    val activitiesIndicator = VisualIndicator.VectorIndicator(activities)
    val freetrainings = Icons.Default.FitnessCenter
    val freetrainingsIndicator = VisualIndicator.VectorIndicator(freetrainings)
    val venues = Icons.Default.HolidayVillage
    val notes = Icons.Default.Description
    val preferences = Icons.Default.Settings
    val manualSync = Icons.Default.Refresh
    val manualSyncIndicator = manualSync.asIndicator
}

val ImageVector.asIndicator get() = VisualIndicator.VectorIndicator(this)

@Suppress("UnusedReceiverParameter")
val Icons.Lsc get() = LscIcons
