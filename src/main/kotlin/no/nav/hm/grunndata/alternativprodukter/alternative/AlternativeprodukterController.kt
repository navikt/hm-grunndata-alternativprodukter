package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import no.nav.hm.grunndata.alternativprodukter.stock.ProductNullableStockDTO
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockDTO
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockRepository
import no.nav.hm.grunndata.alternativprodukter.stock.toNullableDTO

@Controller("/alternativ")
@Tag(name = "Alternativprodukter")
class AlternativeProductsController(
    private val alternativeProductService: AlternativeProductService,
    private val alternativeAndProductStockService: AlternativeAndProductStockService,
    private val productStockRepository: ProductStockRepository
) {

    @Get("/stock-alternatives/{hmsArtNr}")
    suspend fun getStockAndAlternatives(hmsArtNr: String): HttpResponse<ProductStockAlternatives> {
        return HttpResponse.ok(
            //alternativeAndProductStockService.getStockAndAlternativesFromOebs(hmsArtNr),
        )
    }

    @Get("/stock/{hmsArtNr}")
    suspend fun getAlternativeStocks(hmsArtNr: String): HttpResponse<AlternativesWithStock> {
        val alternatives = alternativeProductService.getAlternativeProductsWithoutStock(hmsArtNr)

        val originalStock = productStockRepository.findByHmsArtnr(hmsArtNr)?.toNullableDTO() ?: ProductNullableStockDTO(hmsArtNr, null)

        val alternativeStocks = alternatives.map { productStockRepository.findByHmsArtnr(it)?.toNullableDTO() ?: ProductNullableStockDTO(it, null) }

        return HttpResponse.ok(
            AlternativesWithStock(originalStock, alternativeStocks)
        )
    }

    @Get("/simple/{hmsArtNr}")
    suspend fun getAlternativeProductsWithoutStock(hmsArtNr: String): HttpResponse<List<String>> {

        return HttpResponse.ok(
            alternativeProductService.getAlternativeProductsWithoutStock(hmsArtNr),
        )
    }

    @Get("/mappings")
    fun getAlternativeMappings(): Flow<HmsArtnrMappingInputDTO> {
        return alternativeProductService.getAlternativeMappings().flowOn(Dispatchers.IO)
    }

}

@Serdeable
data class AlternativesWithStock(val original: ProductNullableStockDTO, val alternatives: List<ProductNullableStockDTO>)


@Serdeable
data class ProductStockAlternatives(val original: ProductStockDTO, val alternatives: List<String>)
