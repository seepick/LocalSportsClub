package seepick.localsportsclub

import java.io.InputStream

private object ResourceLocator

fun readFromClasspath(classpath: String): String {
    val resource = openFromClasspath(classpath)
    return resource.bufferedReader().use {
        it.readText()
    }
}

fun openFromClasspath(classpath: String): InputStream {
    return ResourceLocator::class.java.getResourceAsStream(classpath)
        ?: throw Exception("Classpath resource not found at: [$classpath]")
}
