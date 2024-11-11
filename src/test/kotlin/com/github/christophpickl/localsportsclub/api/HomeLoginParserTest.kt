package com.github.christophpickl.localsportsclub.api

import io.kotest.matchers.shouldBe
import org.junit.Test

class HomeLoginParserTest {
    @Test
    fun testFoo() {
        val html = readFromClasspath("/lsc/response_home.html")
        val result = HomeLoginParser.parse(html)
        result.loginSecret shouldBe ("UWZZNDJwNmEvaS9YTHZHN01XQ2QxQT09" to "SlVBNHExWEQ4bncyQTZiRnBrcVNYQT09")
    }
}

private object ResourceLocator
fun readFromClasspath(classpath: String): String {
    val resource = ResourceLocator::class.java.getResourceAsStream(classpath)
        ?: throw Exception("Classpath resource not found at: $classpath")
    return resource.bufferedReader()
        .use {
            it.readText()
        }
}