package seepick.localsportsclub

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.skiko.SystemTheme
import org.jetbrains.skiko.currentSystemTheme
import seepick.localsportsclub.view.common.LscIcons

// https://mdigi.tools/lighten-color/#337be2

// see ColorScheme for description of colors
interface LscColors {
    // btn bg (smooth white): #
// btn disabled bg: A3BCED
// btn disabled text color: white over 95% alpha
    val isFavorited: Color get() = Color(0x66C12600)
    val isWishlisted: Color get() = Color(0x66D0C742)
    val backgroundToolip: Color

    // custom
    val itemHoverBg: Color get() = primaryBrighter

    // custom
    val itemSelectedBg: Color get() = primaryDarker

    // button bg
    val primary: Color

    // custom
    val primaryBrighter: Color

    // custom
    val primaryDarker: Color
    val primaryVariant: Color
    val onPrimary: Color

    val background: Color

    // custom: for alternating row color in lists/tables; background color for area next to tab rows
    val backgroundVariant: Color

    // regular text button (not for button text)
    val onBackground: Color

    // checkbox selected
    val secondary: Color

    // switch selected
    val secondaryVariant: Color
    val onSecondary: Color

    // buttons bg, table header, tick in checkboxes
    val surface: Color

    // borders/bg inputfields, borders table, labels, disabled buttons, outline checkboxes, snackbar bg
    val onSurface: Color

    val error: Color
    val onError: Color

    val scrollbarHover: Color get() = onBackground.copy(alpha = 0.6f)
    val scrollbarUnhover: Color get() = onBackground.copy(alpha = 0.3f)

    // for CheckboxTexted hover bg color
    val hoverIndicator: Color get() = onBackground // alpha will be calculated internally
}

private val colorUnset = Color.Red

object DarkLscColors : LscColors {
    override val primary = Color(0xFF5c95e8)
    override val primaryBrighter = Color(0xFF6CA0EA)
    override val primaryDarker = Color(0xFF337BE2)
    override val primaryVariant = colorUnset
    override val onPrimary = Color(0xFFF9F9F9)
    override val secondary = primary
    override val secondaryVariant = primary
    override val onSecondary = colorUnset
    override val background = Color(0xFF1E1F22)
    override val backgroundVariant = Color(0xFF26282B)
    override val onBackground = Color.White
    override val surface = Color(0xFF2A2C2F)
    override val onSurface = Color(0xFF48494B)
    override val error = colorUnset
    override val onError = colorUnset
    override val backgroundToolip = backgroundVariant
    //  = Color(0xFF)
}

object LightLscColors : LscColors {
    override val primary = Color(0xFF5C95E8)
    override val primaryBrighter = Color(0xFF6CA0EA)
    override val primaryDarker = Color(0xFF337BE2)
    override val primaryVariant = colorUnset
    override val onPrimary = Color(0xFFF9F9F9)
    override val secondary = primary
    override val secondaryVariant = primary
    override val onSecondary = colorUnset
    override val background = Color.White
    override val backgroundVariant = Color(0xFFF9F9F9)
    override val onBackground = Color.Black
    override val surface = Color(0xFFF9F9F9)
    override val onSurface = Color(0xFF48494B)
    override val error = colorUnset
    override val onError = colorUnset
    override val backgroundToolip = Color(255, 255, 210)
}

object Lsc {
    val isDarkTheme2 = currentSystemTheme == SystemTheme.DARK
    val isDarkTheme = false
    val colors: LscColors = if (isDarkTheme) DarkLscColors else LightLscColors
    val icons = LscIcons
}

// access via: Lsc.colors.primary (standard way: MaterialTheme.colors.primary)
@Composable
fun LscTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = Colors(
        primary = Lsc.colors.primary,
        primaryVariant = Lsc.colors.primaryVariant,
        secondary = Lsc.colors.secondary,
        secondaryVariant = Lsc.colors.secondaryVariant,
        background = Lsc.colors.background,
        surface = Lsc.colors.surface,
        error = Lsc.colors.error,
        onPrimary = Lsc.colors.onPrimary,
        onSecondary = Lsc.colors.onSecondary,
        onBackground = Lsc.colors.onBackground,
        onSurface = Lsc.colors.onSurface,
        onError = Lsc.colors.onError,
        isLight = darkTheme,
    )
    val typography = Typography(
        body1 = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = Lsc.colors.onBackground,
        ),
        h1 = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
        ),
        button = TextStyle(
            fontWeight = FontWeight.Thin,
            color = Lsc.colors.onPrimary,
        )
    )
//    val shapes = Shapes(...)
    MaterialTheme(
        colors = colors,
        typography = typography,
        content = content,
    )
}
