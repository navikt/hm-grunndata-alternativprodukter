package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockDTO

@Controller("/alternativ")
@Tag(name = "Alternativprodukter")
class AlternativeProductsController(
    private val alternativeProductService: AlternativeProductService,
) {

    @Get("/stock-alternatives/{hmsArtNr}")
    suspend fun getStockAndAlternatives(hmsArtNr: String): HttpResponse<ProductStockAlternatives> {
        return HttpResponse.ok(
            alternativeProductService.getStockAndAlternativesFromOebs(hmsArtNr),
        )
    }

    @Get("/simple/{hmsArtNr}")
    suspend fun getAlternativeProductsWithoutStock(hmsArtNr: String): HttpResponse<List<String>> {

        return HttpResponse.ok(
            alternativeProductService.getAlternativeProductsWithoutStock(hmsArtNr),
        )
    }
    @Get("/mappings")
    fun getAlternativeMappings(): Flow<HmsArtnrMappingDto> {
        return alternativeProductService.getAlternativeMappings().flowOn(Dispatchers.IO)
    }

}


@Serdeable
data class ProductStockAlternatives(val original: ProductStockDTO, val alternatives: List<String>)
