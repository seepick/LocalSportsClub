package seepick.localsportsclub

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.oshai.kotlinlogging.KotlinLogging.logger

private val log = logger {}

// access via: MaterialTheme.colors.primary
@Composable
fun LscTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) {
        log.debug { "Dark theme enabled" }
        Colors(
            primary = Color(0xFFBB86FC),
            primaryVariant = Color(0xFF3700B3),
            secondary = Color(0xFF03DAC6),
            secondaryVariant = Color(0xFF03DAC6),
            background = Color(0xFF292E36),
            surface = Color(0xFF121212),
            error = Color(0xFFCF6679),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White,
            onError = Color.Black,
            isLight = false,
        )
    } else {
        log.debug { "Light theme enabled" }
        Colors(
            primary = Color(0xFF6200EE),
            primaryVariant = Color(0xFF3700B3),
            secondary = Color(0xFF03DAC6),
            secondaryVariant = Color(0xFF018786),
            background = Color.White,
            surface = Color.White,
            error = Color(0xFFB00020),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color.Black,
            onSurface = Color.Black,
            onError = Color.White,
            isLight = true,
        )
    }
    val typography = Typography(
        body1 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
        ),
        button = TextStyle(
            fontWeight = FontWeight.Thin,
            color = Color.Black,
        )
    )
//    val shapes = Shapes(...)
    MaterialTheme(
        colors = colors,
        typography = typography,
        content = content,
    )
}
