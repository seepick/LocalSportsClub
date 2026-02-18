package seepick.localsportsclub.service

import java.util.Locale

@Deprecated("use usc-client")
fun String.unescape(): String =
    replace("\\\"", "\"").replace("\\n", "\n")

fun String.firstUpper(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
