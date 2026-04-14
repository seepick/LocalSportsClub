import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class UtilsKtTest : DescribeSpec() {
    init {
        describe("String.ensureMaxLength") {
            it("split") {
                "12345xyz".ensureMaxLength(5) shouldBe "12345\nxyz"
            }
            it("too short") {
                "123".ensureMaxLength(5) shouldBe "123"
            }
            it("multi line") {
                "123456\nxy".ensureMaxLength(5) shouldBe "12345\n6\nxy"
            }
        }
    }
}

private fun String.ensureMaxLength(maxLength: Int): String =
    lines().joinToString("\n") { line ->
        var leftOver = line
        val tmp = StringBuilder()
        do {
            val eat = leftOver.take(maxLength)
            leftOver = leftOver.drop(eat.length)
            tmp.append(eat)
            if (leftOver.isNotEmpty()) tmp.appendLine()
        } while (leftOver.isNotEmpty())
        tmp
    }
