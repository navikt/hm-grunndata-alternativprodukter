package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer

@Controller("/stock")
class ProductStockController(private val productStockService: ProductStockService, private val indexer: AlternativeProductIndexer) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @Get("/{hmsArtNr}")
    fun getStockByHmsArtNr(hmsArtNr: String): ProductStockDTO  {
        val productStockDTO =  productStockService.findByHmsArtnr(hmsArtNr)
        coroutineScope.launch {
            indexer.reIndexByHmsNr(hmsArtNr)
        }
        return productStockDTO
    }

    @Get("/{location}/{hmsArtNr}")
    fun getStockByEnhetAndHmsArtNr(location: String, hmsArtNr: String): ProductStockDTO  {
        val productStock = productStockService.findByHmsArtnr(hmsArtNr)
        val filtered = productStock.stockQuantity.filter { it.location == location }
        coroutineScope.launch {
            indexer.reIndexByHmsNr(hmsArtNr)
        }
        return productStock.copy(stockQuantity = filtered)
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(ProductStockController::class.java)
    }

}