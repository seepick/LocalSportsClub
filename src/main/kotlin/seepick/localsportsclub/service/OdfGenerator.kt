package seepick.localsportsclub.service

import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.odftoolkit.simple.SpreadsheetDocument
import org.odftoolkit.simple.style.Font
import org.odftoolkit.simple.style.StyleTypeDefinitions
import java.io.File

object OdfGenerator {

    private val log = logger {}

    fun <T> generate(items: List<T>, table: OdfTable<T>, target: File) {
        val document = SpreadsheetDocument.newSpreadsheetDocument()
        val sheet = document.getSheetByIndex(0)
        sheet.tableName = "LSC Export"

        val headerFont = Font("Arial", StyleTypeDefinitions.FontStyle.BOLD, 14.0)
        val regularFont = Font("Arial", StyleTypeDefinitions.FontStyle.REGULAR, 10.0)

        table.columns.forEachIndexed { colIndex, column ->
            val cell = sheet.getCellByPosition(colIndex, 0)
            cell.styleHandler.setFont(headerFont)
            cell.stringValue = column.header
        }
        table.columns.forEachIndexed { colIndex, column ->
            items.forEachIndexed { itemIndex, item ->
                val cell = sheet.getCellByPosition(colIndex, itemIndex + 1)
                cell.styleHandler.setFont(regularFont)
                when (column.cell) {
                    is OdfDoubleCell<T> -> cell.doubleValue = column.cell.doubleValue(item)
                    is OdfStringCell<T> -> cell.stringValue = column.cell.stringValue(item)
                }
            }
        }

        log.debug { "Saving ODF document to: ${target.absolutePath}" }
        document.save(target)
        document.close()
    }
}

data class OdfTable<T>(
    val columns: List<OdfColumn<T>>,
)

data class OdfColumn<T>(
    val header: String,
    val cell: OdfCell<T>,
) {
    companion object {
        fun <T> byString(header: String, stringValue: (T) -> String) =
            OdfColumn(header, OdfStringCell(header, stringValue))

        fun <T> byDouble(header: String, doubleValue: (T) -> Double) =
            OdfColumn(header, OdfDoubleCell(header, doubleValue))
    }
}

sealed class OdfCell<T>(
    val header: String,
)

class OdfStringCell<T>(
    header: String,
    val stringValue: (T) -> String,
) : OdfCell<T>(header)

class OdfDoubleCell<T>(
    header: String,
    val doubleValue: (T) -> Double,
) : OdfCell<T>(header)
