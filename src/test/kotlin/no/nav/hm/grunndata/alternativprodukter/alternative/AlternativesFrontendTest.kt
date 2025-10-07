package no.nav.hm.grunndata.alternativprodukter.alternative

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.equality.shouldNotBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.MockFactory
import org.junit.jupiter.api.Test


@MicronautTest
class AlternativesFrontendTest(private val alternativesFrontend: AlternativesFrontend) {

    val testHmsnr = MockFactory.testHmsNr

    @Test
    fun `happy path original product with no alternatives`() {
        runBlocking {
            val result = alternativesFrontend.getAlternatives(testHmsnr)
            result.shouldNotBeNull()
            val original = result.original
            val productResponse = ProductResponse(
                seriesId = "seriesid123",
                id = "123",
                seriesTitle = "Produkttittel",
                variantTitle = "Artikkelnavn",
                status = "ACTIVE",
                hmsArtNr = testHmsnr,
                imageUri = null,
                supplierName = "Supplier AS",
                highestRank = 99,
                onAgreement = false,
                warehouseStock = emptyList(),
                inStockAnyWarehouse = false
            )
            original.shouldBeEqualToComparingFields(productResponse)
        }
    }

    @Test
    fun `mismatched expected productResponse should not be equal`() {
        runBlocking {
            val result = alternativesFrontend.getAlternatives(testHmsnr)
            result.shouldNotBeNull()
            val original = result.original
            val productResponse = ProductResponse(
                seriesId = null,
                id = "123",
                seriesTitle = "Produkttittel",
                variantTitle = "Artikkelnavn",
                status = "ACTIVE",
                hmsArtNr = testHmsnr,
                imageUri = null,
                supplierName = "Supplier AS",
                highestRank = 99,
                onAgreement = false,
                warehouseStock = emptyList(),
                inStockAnyWarehouse = false
            )

            original.shouldNotBeEqualToComparingFields(productResponse)
        }
    }

    @Test
    fun `unknown hmsnr should be null`() {
        runBlocking {
            val result = alternativesFrontend.getAlternatives("00000")
            result.shouldBeNull()
        }
    }
}