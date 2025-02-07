package seepick.localsportsclub.api.activity

import kotlinx.serialization.Serializable
import org.jsoup.Jsoup

fun cleanActivityFreetrainingName(input: String): String {
    var htmlInput = input
    var oldHtmlInput: String
    do {
        oldHtmlInput = htmlInput
        htmlInput = Jsoup.parse(oldHtmlInput).text()
    } while (oldHtmlInput != htmlInput)
    return htmlInput.trim()
}

@Serializable
data class ActivityDataLayerClassJson(
    val id: String,
    val name: String,
    val category: String,
    val spots_left: String,
)
