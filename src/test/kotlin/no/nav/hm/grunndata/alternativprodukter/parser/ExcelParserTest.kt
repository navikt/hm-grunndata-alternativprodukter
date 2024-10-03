package no.nav.hm.grunndata.alternativprodukter.parser

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExcelParserTest {
    @Test
    fun `reads Excel and returns groups to be added`() {
        val filePath = "src/test/resources/substituttlister/test_substituttlister.xlsx"
        val excelParser = ExcelParser()
        val parseResult = excelParser.readExcel(filePath)
        assertTrue(parseResult.addGroups.isNotEmpty())
        assertEquals(6, parseResult.addGroups.size)
    }

    @Test
    fun `reads Excel and returns groups to be added and removed`() {
        val filePath = "src/test/resources/substituttlister/test_substituttlister_sletting.xlsx"
        val excelParser = ExcelParser()
        val parseResult = excelParser.readExcel(filePath)
        assertTrue(parseResult.addGroups.isEmpty())
        assertTrue(parseResult.removeGroups.isNotEmpty())
        parseResult.removeGroups.size shouldBe 1
        parseResult.removeGroups.get(0).size shouldBe 3
        parseResult.removeGroups.size shouldBe 1
    }

    @Test
    fun `reads personløftere V1 without exception being thrown`() {
        val filePath = "src/test/resources/substituttlister/test_personloftere_V1.xlsx"
        val excelParser = ExcelParser()
        val parseResult = excelParser.readExcel(filePath)
        assertTrue(parseResult.addGroups.isNotEmpty())
        assertEquals(6, parseResult.addGroups.size)

        parseResult.addGroups.forEach {
           assertTrue(it.size > 1)
        }
    }
}
