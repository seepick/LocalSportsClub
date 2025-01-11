package seepick.localsportsclub.view

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.ImageBitmap

object LscIcons {
    private fun loadIcon(filename: String): ImageBitmap = readImageBitmapFromClasspath("/icons/$filename")
    val FavoriteFull = loadIcon("favorite_full.png")
    val FavoriteOutline = loadIcon("favorite_outline.png")
    val Favorites = FavoriteFull to FavoriteOutline
    val WishlistFull = loadIcon("wishlist_full.png")
    val WishlistOutline = loadIcon("wishlist_outline.png")
    val Wishlists = WishlistFull to WishlistOutline

    const val checkedin = "📝"
    const val booked = "✅" // or scheduled (for freetrainings)
    const val noshow = "❌"
}

val Icons.Lsc get() = LscIcons
