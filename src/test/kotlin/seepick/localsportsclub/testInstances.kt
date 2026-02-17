package seepick.localsportsclub

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.string
import java.net.URL

fun Arb.Companion.url() = arbitrary {
    URL(
        "https://${
            string(
                minSize = 5,
                maxSize = 15,
                codepoints = Codepoint.alphanumeric()
            ).bind()
        }.${string(minSize = 3, maxSize = 3, codepoints = Codepoint.az()).bind()}"
    )
}

fun Arb.Companion.imageUrl() = arbitrary {
    val fileName =
        string(minSize = 3, maxSize = 15, codepoints = Codepoint.az()).bind() + ".png"
    URL("${url().bind()}/${fileName}")
}

fun Arb.Companion.slug() = arbitrary {
    string(minSize = 3, maxSize = 8, codepoints = Codepoint.alphanumeric()).bind()
}
