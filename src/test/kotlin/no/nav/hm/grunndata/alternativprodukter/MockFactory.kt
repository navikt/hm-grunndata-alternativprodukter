package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.mockk.coEvery
import io.mockk.mockk
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.alternativprodukter.alternative.SearchService
import no.nav.hm.grunndata.alternativprodukter.alternative.searchBodyProduct
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer
import no.nav.hm.grunndata.alternativprodukter.index.GdbApiClient
import no.nav.hm.grunndata.alternativprodukter.index.IsoCategoryService
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsStockResponse
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsWarehouseService
import no.nav.hm.grunndata.rapid.dto.Attributes
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import no.nav.hm.grunndata.rapid.dto.SupplierInfo
import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import org.intellij.lang.annotations.Language


@Factory
class MockFactory {

    companion object {
        val sourceHmsNr = "123456789"
        val targetHmsNr = "987654321"
        val testHmsNr = "147286"
    }

    @Singleton
    @Replaces
    fun rapidPushService(): RapidPushService = mockk(relaxed = true)

    @Singleton
    @Replaces
    fun mockGdbApiClient(): GdbApiClient = mockk<GdbApiClient>(relaxed = true).apply {
        coEvery {
            findProductByHmsArtNr(sourceHmsNr)
        } coAnswers {
            ProductRapidDTO(
                id = UUID.randomUUID(),
                hmsArtNr = sourceHmsNr,
                title = "Mock Product",
                articleName = "Mock Article",
                attributes = Attributes(),
                identifier = "mock-identifier",
                supplierRef = "mock-supplier-ref",
                isoCategory = "123456",
                createdBy = "test-user",
                updatedBy = "test-user",
                status = ProductStatus.ACTIVE,
                created = LocalDateTime.now(),
                updated = LocalDateTime.now(),
                expired = LocalDateTime.now(),
                media = setOf<MediaInfo>(),
                techData = emptyList(),
                supplier = SupplierDTO(
                    identifier = "mock-supplier-identifier",
                    id = UUID.randomUUID(),
                    name = "Mock Supplier",
                    info = SupplierInfo(),
                    created = LocalDateTime.now(),
                    updated = LocalDateTime.now(),
                    createdBy = "test-user",
                    updatedBy = "test-user",
                )
            )
        }
    }

    @Singleton
    @Replaces
    fun mockIsoCategoryService(): IsoCategoryService = mockk<IsoCategoryService>(relaxed = true).apply {
        coEvery { lookUpCode("123456") } returns
                IsoCategoryDTO(
                    isoCode = "123456",
                    isoTitle = "mock-title",
                    isoText = "mock-text",
                    isoLevel = 4
                )
    }

    @Singleton
    @Replaces
    fun mockOebsWarehouseService(): OebsWarehouseService = mockk<OebsWarehouseService>(relaxed = true).apply {
        val stockResponse = OebsStockResponse(
            erPåLager = true,
            antallPåLager = 1,
            organisasjons_id = 1,
            organisasjons_navn = "Mock Location",
            fysisk = 1,
            minmax = true,
            anmodning = 0,
            intanmodning = 0,
            forsyning = 0,
            lagervare = true,
            tilgjengeligatt = 0,
            tilgjengeligroo = 0,
            tilgjengelig = 0,
            behovsmeldt = 0,
            reservert = 0,
            restordre = 0,
            bestillinger = 0,
            sortiment = true,
            artikkelid = 1234,
            artikkelnummer = sourceHmsNr
        )
        coEvery { getWarehouseStocks(setOf(sourceHmsNr)) } returns listOf(stockResponse)
        coEvery { getWarehouseStockSingle(sourceHmsNr) } returns listOf(stockResponse)
        coEvery { getWarehouseStockForCentral(setOf(sourceHmsNr), enhetnr = "1000") } returns listOf(stockResponse)
    }

    @Singleton
    @Replaces
    fun mockIndexer(): AlternativeProductIndexer = mockk<AlternativeProductIndexer>(relaxed = true)

    @Singleton
    @Replaces
    fun mockSearchService(): SearchService = mockk<SearchService>(relaxed = true).apply {
        @Language("JSON") val searchResponse = """
            {
                "hits": {
                  "total": {
                    "value": 1
                  },
                  "hits": [{
                    "_source": {
                      "id": "123",
                      "supplier": {
                        "id": "123",
                        "identifier": "321",
                        "name": "Supplier AS"
                      },
                      "title": "Produkttittel",
                      "articleName": "Artikkelnavn",
                      "attributes": {
                      },
                      "status": "ACTIVE",
                      "hmsArtNr": $testHmsNr,
                      "identifier": "identifier",
                      "supplierRef": "levArtNr",
                      "seriesId": "seriesid123",
                      "media": [],
                      "expired": "${LocalDateTime.now()}",
                      "agreements": [],
                      "hasAgreement": "false"
                    }
                }]
              }
            }
        """.trimIndent()

        val body = searchBodyProduct(testHmsNr)

        coEvery { searchWithBody(SearchService.PRODUCTS, any(), body) } returns searchResponse
    }
}