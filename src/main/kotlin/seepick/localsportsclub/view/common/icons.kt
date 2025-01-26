package seepick.localsportsclub.view.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.ImageBitmap

@Suppress("ConstPropertyName")
object LscIcons {
    private fun loadIcon(filename: String): ImageBitmap = readImageBitmapFromClasspath("/icons/$filename")
    val FavoriteFull = loadIcon("favorite_full.png")
    val FavoriteOutline = loadIcon("favorite_outline.png")
    val Favorites = FavoriteFull to FavoriteOutline
    val WishlistFull = loadIcon("wishlist_full.png")
    val WishlistOutline = loadIcon("wishlist_outline.png")
    val Wishlists = WishlistFull to WishlistOutline

    const val checkedinEmoji = "📝"
    const val bookedEmoji = "☑️" // or scheduled (for freetrainings)
    const val noshowEmoji = "🚷"
    const val hiddenEmoji = "🙈"

    val activities = Icons.Default.SportsGymnastics
    val freetrainings = Icons.Default.FitnessCenter
    val venues = Icons.Default.HolidayVillage
    val notes = Icons.Default.Description
    val preferences = Icons.Default.Settings
}

@Suppress("UnusedReceiverParameter")
val Icons.Lsc get() = LscIcons
