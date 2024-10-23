package com.github.christophpickl.localsportsclub

private object ResourceLocator

fun readFromClasspath(classpath: String): String {
    val resource = ResourceLocator::class.java.getResourceAsStream(classpath)
        ?: throw Exception("Classpath resource not found at: $classpath")
    return resource.bufferedReader()
        .use {
            it.readText()
        }
}
