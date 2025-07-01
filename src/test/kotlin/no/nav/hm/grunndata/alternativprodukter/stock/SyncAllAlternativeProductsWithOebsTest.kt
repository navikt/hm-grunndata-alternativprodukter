package no.nav.hm.grunndata.alternativprodukter.stock

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.MockFactory
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMapping
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingRepository
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@MicronautTest
class SyncAllAlternativeProductsWithOebsTest(
    private val syncAllAlternativeProductsWithOebs: SyncAllAlternativeProductsWithOebs,
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository
) {



    init {

        runBlocking {
            val saved = hmsArtnrMappingRepository.save(
                HmsArtnrMapping(
                    sourceHmsArtnr = MockFactory.sourceHmsNr,
                    targetHmsArtnr = MockFactory.targetHmsNr,
                    created = LocalDateTime.now(),
                )
            )
        }
    }

    @Test
    fun testReIndexAllAlternativeProductsWithOebs() {
        runBlocking {
            syncAllAlternativeProductsWithOebs.reIndexAllDinstinctHmsNr()
            val productStockAlternatives1 = syncAllAlternativeProductsWithOebs.getStockAndAlternativesFromOebs(MockFactory.sourceHmsNr)
            productStockAlternatives1.original.hmsArtNr shouldBe MockFactory.sourceHmsNr
            productStockAlternatives1.alternatives shouldContain MockFactory.targetHmsNr
            val updated1 = productStockAlternatives1.original.updated
            val productStockAlternatives2 = syncAllAlternativeProductsWithOebs.getStockAndAlternativesFromOebs(MockFactory.sourceHmsNr)
            productStockAlternatives2.original.updated shouldBeAfter updated1
        }
    }

}





