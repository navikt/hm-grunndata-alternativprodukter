package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.mockk.coEvery
import io.mockk.mockk
import jakarta.inject.Singleton
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer
import no.nav.hm.grunndata.alternativprodukter.index.GdbApiClient
import no.nav.hm.grunndata.alternativprodukter.index.IsoCategoryService
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsWarehouseService
import no.nav.hm.grunndata.rapid.dto.Attributes
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.SupplierDTO
import no.nav.hm.grunndata.rapid.dto.SupplierInfo

import no.nav.hm.rapids_rivers.micronaut.RapidPushService
import java.time.LocalDateTime
import java.util.UUID


@Factory
class MockFactory {

    companion object {
        val sourceHmsNr = "123456789"
        val targetHmsNr = "987654321"

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
    fun mockOebsWarehouseService(): OebsWarehouseService = mockk<OebsWarehouseService>(relaxed = true)

    @Singleton
    @Replaces
    fun mockIndexer(): AlternativeProductIndexer = mockk<AlternativeProductIndexer>(relaxed = true)

}