package seepick.localsportsclub.view

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import seepick.localsportsclub.openFromClasspath

object LscIcons {
    private fun loadImageBitmap(filename: String): ImageBitmap = loadImageBitmap(openFromClasspath("/icons/$filename"))
    val FavoriteFull = loadImageBitmap("favorite_full.png")
    val FavoriteOutline = loadImageBitmap("favorite_outline.png")
    val Favorites = FavoriteFull to FavoriteOutline
    val WishlistFull = loadImageBitmap("wishlist_full.png")
    val WishlistOutline = loadImageBitmap("wishlist_outline.png")
    val Wishlists = WishlistFull to WishlistOutline

    const val checkedin = "✅"
    const val booked = "📝"
}

val Icons.Lsc get() = LscIcons
