package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

@Controller("/alternativ")
@Tag(name = "Alternativprodukter")
class AlternativeProductsController(
    private val alternativeProductsService: AlternativeProductsService,
) {
    @Get("/{hmsArtNr}")
    suspend fun getAlternativeProducts(hmsArtNr: String): HttpResponse<AlternativeProductsResponse> {

        return HttpResponse.ok(
            alternativeProductsService.getAlternativeProducts(hmsArtNr),
        )
    }
    @Get("/stock-alternatives/{hmsArtNr}")
    suspend fun getStockAndAlternatives(hmsArtNr: String): HttpResponse<ProductStockAlternatives> {
        return HttpResponse.ok(
            alternativeProductsService.getStockAndAlternatives(hmsArtNr),
        )
    }

    @Get("/simple/{hmsArtNr}")
    suspend fun getAlternativeProductsWithoutStock(hmsArtNr: String): HttpResponse<List<String>> {

        return HttpResponse.ok(
            alternativeProductsService.getAlternativeProductsWithoutStock(hmsArtNr),
        )
    }
    @Get("/mappings")
    fun getAlternativeMappings(): Flow<HmsArtnrMappingDto> {
        return alternativeProductsService.getAlternativeMappings().flowOn(Dispatchers.IO)
    }

}



@Serdeable
data class AlternativeProductsResponse(val original: ProductStock, val alternatives: List<ProductStock>)

@Serdeable
data class ProductStock(val hmsArtNr: String, val warehouseStock: List<WarehouseStockResponse>)

@Serdeable
data class ProductStockAlternatives(val original: ProductStock, val alternatives: List<String>)