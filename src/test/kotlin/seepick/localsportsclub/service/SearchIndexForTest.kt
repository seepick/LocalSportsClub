package seepick.localsportsclub.service

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SearchIndexForTest : StringSpec() {

    init {
        "When adding in the middle" {
            val items = listOf("a", "b", "d")
            val index = findIndexFor(items, "c", { it })

            index shouldBe 2
            items.add(index, "c") shouldBe listOf("a", "b", "c", "d")
        }
        "When adding in the beginning" {
            val items = listOf("b", "c", "d")
            val index = findIndexFor(items, "a", { it })

            index shouldBe 0
            items.add(index, "a") shouldBe listOf("a", "b", "c", "d")
        }
        "When adding in the end" {
            val items = listOf("a", "b", "c")
            val index = findIndexFor(items, "x", { it })

            index shouldBe 3
            items.add(index, "x") shouldBe listOf("a", "b", "c", "x")
        }
        "When same already given Then add behind" {
            val items = listOf("a", "b", "c")
            val index = findIndexFor(items, "b", { it })

            index shouldBe 2
            items.add(index, "b") shouldBe listOf("a", "b", "b", "c")
        }
        "Add to empty list" {
            val items = emptyList<String>()
            val index = findIndexFor(items, "x", { it })
            index shouldBe 0
            items.add(index, "x") shouldBe listOf("x")
        }
    }

    private fun <T> List<T>.add(index: Int, element: T) =
        toMutableList().also { it.add(index, element) }
}
