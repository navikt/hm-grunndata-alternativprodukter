package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/stock")
class ProductStockController(private val productStockService: ProductStockService) {

    @Get("/{hmsArtNr}")
    fun getStockByHmsArtNr(hmsArtNr: String): ProductStockDTO = productStockService.findByHmsArtnr(hmsArtNr)

    @Get("/{location}/{hmsArtNr}")
    fun getStockByEnhetAndHmsArtNr(location: String, hmsArtNr: String): ProductStockDTO  {
        val productStock = productStockService.findByHmsArtnr(hmsArtNr)
        val filtered = productStock.stockQuantity.filter { it.location == location }
        return productStock.copy(stockQuantity = filtered)
    }

}