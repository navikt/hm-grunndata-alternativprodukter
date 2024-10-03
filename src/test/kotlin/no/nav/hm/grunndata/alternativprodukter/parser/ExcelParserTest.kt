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
        assertTrue(parseResult.addGroups.isNotEmpty())
        assertEquals(5, parseResult.addGroups.size)
        parseResult.removeGroups.size shouldBe 2
        parseResult.removeGroups.get(0).size shouldBe 3
        parseResult.removeGroups.size shouldBe 2
        parseResult.removeGroups.get(1).size shouldBe 1
    }
}
