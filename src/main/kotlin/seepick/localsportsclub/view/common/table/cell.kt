package seepick.localsportsclub.view.common.table

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import seepick.localsportsclub.view.common.WidthOrWeight

sealed interface CellValue {
    class StringValue(val string: String) : CellValue
    class AnnotatedStringValue(val annotated: AnnotatedString) : CellValue
    class IntValue(val int: Int) : CellValue

    companion object {
        operator fun invoke(string: String) = StringValue(string)
        operator fun invoke(annotated: AnnotatedString) = AnnotatedStringValue(annotated)
        operator fun invoke(int: Int) = IntValue(int)
    }
}

@Composable
fun RowScope.TableTextCell(
    value: CellValue,
    size: WidthOrWeight,
    textDecoration: TextDecoration? = null,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    modifier: Modifier = Modifier,
    paddingLeft: Boolean = false,
) {
    val finalModifier = applyColSize(Modifier, size)
        .align(Alignment.CenterVertically)
        .let { if (paddingLeft) it.padding(start = 8.dp) else it }
        .then(modifier)

    when (value) {
        is CellValue.AnnotatedStringValue -> Text(
            text = value.annotated,
            textDecoration = textDecoration,
            maxLines = 1,
            textAlign = textAlign,
            fontWeight = fontWeight,
            overflow = TextOverflow.Ellipsis,
            modifier = finalModifier
        )

        is CellValue.StringValue -> Text(
            text = value.string,
            textDecoration = textDecoration,
            maxLines = 1,
            textAlign = textAlign,
            fontWeight = fontWeight,
            overflow = TextOverflow.Ellipsis,
            modifier = finalModifier
        )

        is CellValue.IntValue -> Text(
            text = value.int.toString(),
            textDecoration = textDecoration,
            maxLines = 1,
            textAlign = textAlign,
            fontWeight = fontWeight,
            overflow = TextOverflow.Ellipsis,
            modifier = finalModifier
        )
    }
}

sealed interface CellRenderer<T> {
    data class TextRenderer<T>(
        val valueExtractor: (T) -> CellValue,
        val sortExtractor: (T) -> Any?,
        val textAlign: TextAlign? = null,
        val paddingLeft: Boolean = false,
        val paddingRight: Boolean = false,
    ) : CellRenderer<T> {
        companion object {
            fun <T> forString(
                textAlign: TextAlign? = null,
                paddingLeft: Boolean = false,
                paddingRight: Boolean = false,
                extractor: (T) -> String,
            ): TextRenderer<T> = TextRenderer(
                valueExtractor = { CellValue(extractor(it)) },
                sortExtractor = extractor,
                textAlign = textAlign,
                paddingLeft = paddingLeft,
                paddingRight = paddingRight,
            )

            fun <T> forInt(
                textAlign: TextAlign? = null,
                paddingLeft: Boolean = false,
                paddingRight: Boolean = false,
                extractor: (T) -> Int,
            ): TextRenderer<T> = TextRenderer(
                valueExtractor = { CellValue(extractor(it)) },
                sortExtractor = extractor,
                textAlign = textAlign,
                paddingLeft = paddingLeft,
                paddingRight = paddingRight,
            )
        }
    }

    data class CustomRenderer<T>(val invoke: @Composable RowScope.(T, TableColumn<T>) -> Unit) : CellRenderer<T>
}

fun RowScope.applyColSize(mod: Modifier, size: WidthOrWeight) = mod.let {
    when (size) {
        is WidthOrWeight.Weight -> mod.weight(size.value, true)
        is WidthOrWeight.Width -> mod.width(size.value)
    }
}
