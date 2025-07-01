package no.nav.hm.grunndata.alternativprodukter.stock

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.alternativprodukter.MockFactory
import org.junit.jupiter.api.Test

@MicronautTest
class FetchOebsAndIndexProductStockComponentTest(private val fetchOebsAndIndexProductStockComponent: FetchOebsAndIndexProductStockComponent) {

    @Test
    fun testFetchOebsAndIndexProductStock() {
        val stocks = fetchOebsAndIndexProductStockComponent.findByHmsnrs(hmsnrs = setOf(MockFactory.sourceHmsNr))
        stocks.size.shouldBe(1)
        stocks.first().hmsArtNr shouldBe MockFactory.sourceHmsNr
        stocks.first().warehouseStock.size shouldBe 1
        stocks.first().warehouseStock.first().inStock shouldBe true
        stocks.first().warehouseStock.first().amountInStock shouldBe 1
        stocks.first().warehouseStock.first().intRequest shouldBe 0
    }
}