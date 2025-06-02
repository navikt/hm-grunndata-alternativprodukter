package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer

@Controller("/stock")
class ProductStockController(private val productStockService: ProductStockService, private val indexer: AlternativeProductIndexer) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @Get("/{hmsArtNr}")
    fun getStockByHmsArtNr(hmsArtNr: String): ProductStockDTO? {
        val productStockDTO =  productStockService.findByHmsArtnr(hmsArtNr)
        if (productStockDTO!=null) {
            coroutineScope.launch {
                indexer.reIndexByHmsNr(hmsArtNr)
            }
        }
        return productStockDTO
    }

    @Get("/{location}/{hmsArtNr}")
    fun getStockByEnhetAndHmsArtNr(location: String, hmsArtNr: String): ProductStockDTO? {
        val productStock = productStockService.findByHmsArtnr(hmsArtNr)
        if (productStock != null) {
            val filtered = productStock.warehouseStock.filter { it.location == location }
            coroutineScope.launch {
                indexer.reIndexByHmsNr(hmsArtNr)
            }
           return productStock.copy(warehouseStock = filtered)
        }
        return null
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(ProductStockController::class.java)
    }

}