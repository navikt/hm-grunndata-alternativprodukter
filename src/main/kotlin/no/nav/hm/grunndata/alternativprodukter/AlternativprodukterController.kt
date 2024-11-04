package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory

@Controller("/alternativ")
@Tag(name = "Alternativprodukter")
class AlternativeProductsController(
    private val AlternativeProductsService: AlternativeProductsService,
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductsController::class.java)
    }

    @Get("/{hmsArtNr}")
    suspend fun getAlternativeProducts(hmsArtNr: String): HttpResponse<AlternativeProductsResponse> {
        LOG.debug("Getting alternative products for hmsArtNr: $hmsArtNr")
        return HttpResponse.ok(
            AlternativeProductsService.getAlternativeProducts(hmsArtNr),
        )
    }
    @Get("/stock-alternatives/{hmsArtNr}")
    suspend fun getStockAndAlternatives(hmsArtNr: String): HttpResponse<ProductStockAlternatives> {
        return HttpResponse.ok(
            AlternativeProductsService.getStockAndAlternatives(hmsArtNr),
        )
    }
}

@Serdeable
data class AlternativeProductsResponse(val original: ProductStock, val alternatives: List<ProductStock>)

@Serdeable
data class ProductStock(val hmsArtNr: String, val warehouseStock: List<WarehouseStockResponse>)

@Serdeable
data class ProductStockAlternatives(val original: ProductStock, val alternatives: List<String>)