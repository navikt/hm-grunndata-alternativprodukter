package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/stock")
class ProductStockController(private val productStockService: ProductStockService) {

    @Get("/{hmsArtNr}")
    fun getStockByHmsArtNr(hmsArtNr: String): ProductStock = productStockService.findByHmsArtnr(hmsArtNr)

    @Get("/{enhet}/{hmsArtNr}")
    fun getStockByEnhetAndHmsArtNr(enhet: String, hmsArtNr: String): ProductStock  {
        val filtered = productStockService.findByHmsArtnr(hmsArtNr).warehouseStockResponse.filter { it.organisasjons_navn == enhet }
        return ProductStock( hmsArtnr=hmsArtNr, warehouseStockResponse = filtered)
    }

}