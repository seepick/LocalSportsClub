package seepick.localsportsclub.service

import java.io.File

object OdfGeneratorApp {
    @JvmStatic
    fun main(args: Array<String>) {
        OdfGenerator.generate(
            items = listOf(
                OdfItem("Foo", 1.0),
                OdfItem("Bar", 42.0),
            ), table = OdfTable(
                columns = listOf(
                    OdfColumn.byString("Name") { it.name },
                    OdfColumn.byDouble("Number") { it.number },
                ),
            ), target = File("output.ods")
        )
    }
}

private data class OdfItem(
    val name: String,
    val number: Double,
)
