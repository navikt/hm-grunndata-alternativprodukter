package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import no.nav.hm.grunndata.alternativprodukter.stock.ProductNullableStockDTO
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockRepository
import no.nav.hm.grunndata.alternativprodukter.stock.toNullableDTO
import org.slf4j.LoggerFactory

@Controller("/alternativ")
@Tag(name = "Alternativprodukter")
class AlternativeProductsController(
    private val alternativeProductService: AlternativeProductService,
    private val alternativesFrontend: AlternativesFrontend,
    private val productStockRepository: ProductStockRepository,
    private val azureAdUserClient: AzureAdUserClient
) {

    companion object {
        val LOG = LoggerFactory.getLogger(AlternativeProductsController::class.java)
    }

    @Get("/stock/{hmsArtNr}")
    suspend fun getAlternativeStocks(hmsArtNr: String): HttpResponse<AlternativesWithStock> {
        val alternatives = alternativeProductService.getAlternativeProductsWithoutStock(hmsArtNr)

        val originalStock =
            productStockRepository.findByHmsArtnr(hmsArtNr)?.toNullableDTO() ?: ProductNullableStockDTO(hmsArtNr, null)

        val alternativeStocks = alternatives.map {
            productStockRepository.findByHmsArtnr(it)?.toNullableDTO() ?: ProductNullableStockDTO(
                it,
                null
            )
        }

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

    @Get("/alternatives/{hmsArtNr}")
    suspend fun getAlternatives(
        @Header("Authorization") authorization: String,
        hmsArtNr: String
    ): HttpResponse<AlternativesWithStockNew> {
        val authToken = authorization.removePrefix("Bearer ")

        val tokenValidated = azureAdUserClient.validateToken(AuthBody(token = authToken))

        if (tokenValidated.active) {
            val alternativesWithStockNew = try {
                alternativesFrontend.getAlternatives(hmsArtNr)
            } catch (illegalArgument: IllegalArgumentException) {
                return HttpResponse.notFound()
            }

            return HttpResponse.ok(alternativesWithStockNew)
        } else {
            LOG.warn("Token fail: " + tokenValidated.error)
            LOG.warn("Token: " + tokenValidated)
            return HttpResponse.unauthorized()
        }
    }
}

@Serdeable
data class AlternativesWithStock(val original: ProductNullableStockDTO, val alternatives: List<ProductNullableStockDTO>)