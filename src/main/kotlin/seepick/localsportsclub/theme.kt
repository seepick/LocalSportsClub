package seepick.localsportsclub

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.CheckboxColors
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
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

    val cancelBookingWithin: Color
    val cancelBookingOutside: Color

    val isFavorited: Color get() = Color(0x66C12600)
    val isWishlisted: Color get() = Color(0x66D0C742)
    val backgroundToolip: Color

    // custom
    val itemHoverBg get() = primaryBrighter
    val clickableNeutral get() = primaryBrighter
    val clickableSelected get() = primaryDarker

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
    val onSurfaceAlphaLow: Color get() = onSurface.copy(alpha = 0.3f)

    val error: Color
    val onError: Color

    val scrollbarHover: Color get() = clickableNeutral.copy(alpha = 0.6f)
    val scrollbarUnhover: Color get() = clickableNeutral.copy(alpha = 0.3f)

    // for CheckboxTexted hover bg color
    val hoverIndicator: Color get() = onBackground // alpha will be calculated internally


    val checkins: Color get() = Color(0xFF0076BA)
    val booked: Color get() = Color(0xFF00A2FF)
    val available: Color get() = Color(0xFF6AC43C)
    val backgroundGray: Color get() = Color(0xFF9B9B9B)

    val remarkRatingAmazing: Color
    val remarkRatingGood: Color
    val remarkRatingMeh: Color
    val remarkRatingBad: Color
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
    override val cancelBookingWithin = Color(0xFF3EDE5F)
    override val cancelBookingOutside = Color(0xFFEA6065)

    override val remarkRatingAmazing = Color(0xFF20A819)
    override val remarkRatingGood = Color(0xFF4F7444)
    override val remarkRatingMeh = Color(0xFF927444)
    override val remarkRatingBad = Color(0xFF924944)
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
    override val cancelBookingWithin = Color(0xFF4CA631)
    override val cancelBookingOutside = Color(0xFFB3192A)

    override val remarkRatingAmazing = Color(0xFF20A819)
    override val remarkRatingGood = Color(0xFF4F7444)
    override val remarkRatingMeh = Color(0xFF927444)
    override val remarkRatingBad = Color(0xFF924944)
}

object Lsc {
    val isDarkTheme = currentSystemTheme == SystemTheme.DARK
    val colors: LscColors = if (isDarkTheme) DarkLscColors else LightLscColors
    val icons = LscIcons
}

val LocalTextFieldColors = compositionLocalOf<TextFieldColors> {
    error("No text field could be found")
}
val LocalCheckboxColors = compositionLocalOf<CheckboxColors> {
    error("No checkbox could be found")
}

// TextField(...
//        colors = androidx.compose.material.TextFieldDefaults.textFieldColors(
//            backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
//        ),

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
    val textFieldColors = TextFieldDefaults.textFieldColors(
        textColor = Lsc.colors.onBackground,
        backgroundColor = Color.Transparent,
        focusedIndicatorColor = Lsc.colors.clickableSelected,
        unfocusedIndicatorColor = Lsc.colors.clickableNeutral,
        focusedLabelColor = Lsc.colors.onBackground,
        unfocusedLabelColor = Lsc.colors.onBackground,
    )

    val checkboxFieldColors = CheckboxDefaults.colors(
        uncheckedColor = Lsc.colors.clickableNeutral,
        checkedColor = Lsc.colors.clickableSelected,
    )
    CompositionLocalProvider(
        LocalTextFieldColors provides textFieldColors,
        LocalCheckboxColors provides checkboxFieldColors
    ) {
        MaterialTheme(
            colors = colors,
            typography = typography,
            content = content,
        )
    }
}
