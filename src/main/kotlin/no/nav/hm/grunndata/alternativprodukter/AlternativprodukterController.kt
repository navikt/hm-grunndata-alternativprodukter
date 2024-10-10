package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/alternativ")
@Tag(name = "Alternativprodukter")
class AlternativeProductsController(
    private val AlternativeProductsService: AlternativeProductsService,
) {
    @Get("/{hmsArtNr}")
    suspend fun getAlternativeProducts(hmsArtNr: String): HttpResponse<List<AlternativeProduct>> {

        return HttpResponse.ok(
            AlternativeProductsService.getAlternativeProducts(hmsArtNr),
        )
    }
}

@Serdeable
data class AlternativeProduct(val hmsArtNr: String, val warehouseStock: List<WarehouseStockResponse>)