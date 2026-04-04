package seepick.localsportsclub.view.venue.detail

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import seepick.localsportsclub.service.model.RemarkRating
import seepick.localsportsclub.view.remark.withColor

fun buildXString(code: XString.Builder.() -> Unit): XString =
    XString.Builder(StringBuilder(), AnnotatedString.Builder())
        .apply(code)
        .build()

interface XString {
    val asString: String
    val asAnnotatedString: AnnotatedString


    companion object {
        operator fun invoke(string: String?): XString? =
            if (string == null) null else XSimpleString(string)
    }

    class Builder(
        private val stringBuilder: StringBuilder,
        private val annotatedBuilder: AnnotatedString.Builder,
    ) {

        fun append(string: String) {
            stringBuilder.append(string)
            annotatedBuilder.append(string)
        }

        fun withStyle(style: SpanStyle, block: Builder.() -> Unit) {
            annotatedBuilder.withStyle(style, { block() })
        }

        fun withColor(rating: RemarkRating?, block: Builder.() -> Unit) {
            annotatedBuilder.withColor(rating, { block() })
        }

        fun build() = XStringImpl(stringBuilder, annotatedBuilder)
    }
}

class XStringImpl(stringBuilder: StringBuilder, annotatedBuilder: AnnotatedString.Builder) : XString {
    override val asString by lazy {
        stringBuilder.toString()
    }

    override val asAnnotatedString by lazy {
        annotatedBuilder.toAnnotatedString()
    }
}

class XSimpleString(string: String) : XString {
    override val asString = string
    override val asAnnotatedString = AnnotatedString(string)
}
