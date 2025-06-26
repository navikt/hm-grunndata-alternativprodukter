package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer

@Controller("/stock")
class ProductStockController(private val fetchOebsAndIndexProductStockComponent: FetchOebsAndIndexProductStockComponent, private val indexer: AlternativeProductIndexer) {

    @Get("/{hmsArtNr}")
    fun getStockByHmsArtNr(hmsArtNr: String): ProductStockDTO {
        LOG.info("Getting stock by hmsArtNr: $hmsArtNr")
        val productStockDTO =  fetchOebsAndIndexProductStockComponent.findByHmsArtnr(hmsArtNr)
        return productStockDTO
    }

    @Get("/{location}/{hmsArtNr}")
    fun getStockByEnhetAndHmsArtNr(location: String, hmsArtNr: String): ProductStockDTO {
        LOG.info("Getting stock by hmsArtNr: $hmsArtNr and $location")
        val productStock = fetchOebsAndIndexProductStockComponent.findByHmsArtnr(hmsArtNr)
        val filtered = productStock.warehouseStock.filter { it.location == location }
        return productStock.copy(warehouseStock = filtered)
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(ProductStockController::class.java)
    }

}