package no.nav.hm.grunndata.alternativprodukter.parser

import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class ExcelParser {
    @Throws(IOException::class)
    fun readExcel(inputStream: InputStream): ParseResult {
        val workbook: Workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(1)
        val addGroups: MutableList<List<String>> = ArrayList()
        val currentAddGroup: MutableList<String> = ArrayList()

        val removeGroups: MutableList<List<String>> = ArrayList()
        val currentRemoveGroup: MutableList<String> = ArrayList()

        var isFirstRow = true

        for (row in sheet) {
            // Skip the first row
            if (isFirstRow) {
                isFirstRow = false
                continue
            }
            val cell = row.getCell(3) // Column D
            val removeCell = row.getCell(6) // Column G
            if (cell != null && cell.toString().trim { it <= ' ' }.isNotEmpty()) {
                var cellValue = cell.toString()
                if (cellValue.endsWith(".0")) {
                    cellValue = cellValue.substring(0, cellValue.length - 2)
                }
                if (removeCell != null && removeCell.toString().trim().equals("x", ignoreCase = true)) {
                    currentRemoveGroup.add(cellValue)
                } else {
                    currentAddGroup.add(cellValue)
                }
            } else {
                if (currentAddGroup.isNotEmpty()) {
                    addGroups.add(ArrayList(currentAddGroup))
                    currentAddGroup.clear()
                }
                if (currentRemoveGroup.isNotEmpty()) {
                    removeGroups.add(ArrayList(currentRemoveGroup))
                    currentRemoveGroup.clear()
                }
            }
        }
        if (currentAddGroup.isNotEmpty()) {
            addGroups.add(currentAddGroup)
        }
        if (currentRemoveGroup.isNotEmpty()) {
            removeGroups.add(currentRemoveGroup)
        }
        workbook.close()
        inputStream.close()
        return ParseResult(addGroups, removeGroups)
    }
}

data class ParseResult(
    val addGroups: List<List<String>>,
    val removeGroups: List<List<String>>,
)
