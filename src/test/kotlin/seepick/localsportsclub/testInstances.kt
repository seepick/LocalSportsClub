package seepick.localsportsclub

import io.kotest.property.Arb
import io.kotest.property.arbitrary.Codepoint
import io.kotest.property.arbitrary.alphanumeric
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.az
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.ktor.http.Url

fun Arb.Companion.url() = arbitrary {
    Url(
        "https://${
            string(
                minSize = 5,
                maxSize = 15,
                codepoints = Codepoint.alphanumeric()
            ).next()
        }.${string(minSize = 3, maxSize = 3, codepoints = Codepoint.az()).next()}"
    )
}

fun Arb.Companion.imageUrl() = arbitrary {
    val fileName =
        string(minSize = 3, maxSize = 15, codepoints = Codepoint.az()).next() + ".png"
    Url("${url().next()}/${fileName}")
}

fun Arb.Companion.slug() = arbitrary {
    string(minSize = 3, maxSize = 8, codepoints = Codepoint.alphanumeric()).next()
}
