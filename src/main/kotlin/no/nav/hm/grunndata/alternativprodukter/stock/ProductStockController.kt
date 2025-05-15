package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Controller("/stock")
class ProductStockController(private val productStockService: ProductStockService, private val indexClient: IndexClient) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @Get("/{hmsArtNr}")
    fun getStockByHmsArtNr(hmsArtNr: String): ProductStockDTO  {
        val productStockDTO =  productStockService.findByHmsArtnr(hmsArtNr)
        coroutineScope.launch {
            // not optimal, but works for now.
            indexClient.indexAlternativeProductsByHmsNr(hmsArtNr)
        }
        return productStockDTO
    }

    @Get("/{location}/{hmsArtNr}")
    fun getStockByEnhetAndHmsArtNr(location: String, hmsArtNr: String): ProductStockDTO  {
        val productStock = productStockService.findByHmsArtnr(hmsArtNr)
        val filtered = productStock.stockQuantity.filter { it.location == location }
        coroutineScope.launch {
            indexClient.indexAlternativeProductsByHmsNr(hmsArtNr)
        }
        return productStock.copy(stockQuantity = filtered)
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(ProductStockController::class.java)
    }

}