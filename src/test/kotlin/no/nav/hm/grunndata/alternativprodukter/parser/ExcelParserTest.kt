package no.nav.hm.grunndata.alternativprodukter.parser

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExcelParserTest {


    @Test
    fun `reads stoler liste without exception`() {
        val inputStream =
            this::class.java.classLoader.getResourceAsStream("substituttlister/V2_stoler_med_oppreisingsfunksjon.xlsx")
        val excelParser = ExcelParser()
        val parseResult = excelParser.readExcel(inputStream)
        assertTrue(parseResult.addGroups.isNotEmpty())
        assertEquals(33, parseResult.addGroups.size )
    }

    @Test
    fun `reads personloftere liste without exception`() {
        val inputStream =
            this::class.java.classLoader.getResourceAsStream("substituttlister/V1_personloftere.xlsx")
        val excelParser = ExcelParser()
        val parseResult = excelParser.readExcel(inputStream)
        assertTrue(parseResult.addGroups.isNotEmpty())
        assertEquals(10, parseResult.addGroups.size )
    }

    @Test
    fun `reads kalendere file without exception`() {
        val inputStream =
            this::class.java.classLoader.getResourceAsStream("substituttlister/V3_kalendere_1.xlsx")
        val excelParser = ExcelParser()
        val parseResult = excelParser.readExcel(inputStream)
        assertTrue(parseResult.addGroups.isNotEmpty())
        assertEquals(28, parseResult.addGroups.size )
    }

}
