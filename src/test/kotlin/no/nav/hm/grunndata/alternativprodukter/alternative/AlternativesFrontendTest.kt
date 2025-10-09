package no.nav.hm.grunndata.alternativprodukter.alternative

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.equality.shouldNotBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldNotBeNull
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.MockFactory
import no.nav.hm.grunndata.alternativprodukter.MockFactory.Companion.testHmsNr
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsStockResponse
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStock
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockRepository
import org.junit.jupiter.api.Test


@MicronautTest
class AlternativesFrontendTest(private val alternativesFrontend: AlternativesFrontend) {

    val testHmsnr = MockFactory.testHmsNr

    @MockBean(ProductStockRepository::class)
    fun mockProductStockRepository(): ProductStockRepository = mockk<ProductStockRepository>().apply {
        val oebsStockResponse = OebsStockResponse(
            erPåLager = true,
            antallPåLager = 1,
            organisasjons_id = 1,
            organisasjons_navn = "1234Location",
            fysisk = 1,
            minmax = true,
            anmodning = 0,
            intanmodning = 0,
            forsyning = 0,
            lagervare = true,
            tilgjengeligatt = 0,
            tilgjengeligroo = 0,
            tilgjengelig = 1,
            behovsmeldt = 0,
            reservert = 0,
            restordre = 0,
            bestillinger = 0,
            sortiment = true,
            artikkelid = 1234,
            artikkelnummer = testHmsNr
        )
        val productStock = ProductStock(
            hmsArtnr = testHmsNr,
            oebsStockResponse = listOf(oebsStockResponse),
        )

        coEvery { findByHmsArtnr(testHmsNr) } returns productStock
        coEvery { findByHmsArtnr(neq(testHmsNr)) } answers { callOriginal() }
        coEvery { save(any()) } answers { callOriginal() }
        coEvery { update(any()) } answers { callOriginal() }
    }

    @Test
    fun `happy path original product with no alternatives`() {
        runBlocking {
            val result = alternativesFrontend.getAlternatives(testHmsnr)
            result.shouldNotBeNull()
            val original = result.original
            val productResponse = ProductResponse(
                seriesId = "seriesid123",
                variantId = "123",
                seriesTitle = "Produkttittel",
                variantTitle = "Artikkelnavn",
                status = "ACTIVE",
                hmsArtNr = testHmsnr,
                imageUri = null,
                supplierName = "Supplier AS",
                highestRank = 99,
                onAgreement = false,
                warehouseStock = listOf(StockResponse(location = "Location", available = 1)),
                inStockAnyWarehouse = true
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
                variantId = "123",
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
    fun `unknown hmsnr should throw exception`() {
        runBlocking {
            shouldThrow<IllegalArgumentException> {
                alternativesFrontend.getAlternatives("00000")
            }
        }
    }
}