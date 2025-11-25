package no.nav.hm.grunndata.alternativprodukter.alternative

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.alternativprodukter.index.SearchDoc
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsStockResponse
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockRepository
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import kotlin.math.max

@Singleton
class AlternativesFrontend(
    private val alternativeProductService: AlternativeProductService,
    private val productStockRepository: ProductStockRepository,
    private val searchService: SearchService,
    private val objectMapper: ObjectMapper,
    private val cliqueService: CliqueService,
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativesFrontend::class.java)
    }

    suspend fun getAlternatives(hmsNr: String): AlternativesWithStockNew? {
        val alternatives = alternativeProductService.getAlternativeProductsWithoutStock(hmsNr)

        val originalResponse = searchForProduct(hmsNr)?.toResponse() ?: throw IllegalArgumentException("Unknown hmsNr")
        val alternativesResponse = alternatives.map { searchForProduct(it) }.mapNotNull { it?.toResponse() }

        return AlternativesWithStockNew(original = originalResponse, alternatives = alternativesResponse)

    }

    suspend fun getAlternativesInGroups(hmsNr: String): AlternativesWithStockGrouped? {
        val base = getAlternatives(hmsNr) ?: return null

        // Map hmsArtNr -> ProductResponse for quick lookup
        val original = base.original
        val alternativeByHms = base.alternatives
            .filterNotNull()
            .associateBy { it.hmsArtNr }

        val cliques = cliqueService.findCliquesContaining(hmsNr)

        // Build groups: each clique becomes a group of ProductResponses (original first if present)
        val groups = cliques.map { clique ->
            val products = mutableListOf<ProductResponse>()

            // Always include original first if it is part of this clique
            if (clique.contains(original.hmsArtNr)) {
                products.add(original)
            }

            // Then include alternatives from this clique (excluding original to avoid duplicates)
            clique.filter { it != original.hmsArtNr }
                .mapNotNull { alternativeByHms[it] }
                .forEach { products.add(it) }

            products.toList()
        }.filter { it.isNotEmpty() }

        return AlternativesWithStockGrouped(original = original, groups = groups)
    }

    private fun searchForProduct(hmsNr: String): ProductDoc? {
        val params = emptyMap<String, String>()

        val products = searchService.searchWithBody(SearchService.PRODUCTS, params, searchBodyProduct(hmsNr))

        val json = objectMapper.readTree(products)
        val hits = json.get("hits") ?: return null
        hits.get("total")?.get("value")?.asInt() ?: return null
        return hits.get("hits").map {
            objectMapper.treeToValue(
                it.get("_source"),
                ProductDoc::class.java
            )
        }.firstOrNull()
    }

    suspend fun ProductDoc.toResponse(): ProductResponse {
        val oebsStockResponse = productStockRepository.findByHmsArtnr(hmsArtNr!!)?.oebsStockResponse

        return ProductResponse(
            seriesId = seriesId,
            variantId = id,
            seriesTitle = title,
            variantTitle = articleName,
            status = status.name,
            hmsArtNr = hmsArtNr,
            imageUri = mapToImageUri(media),
            supplierName = supplier.name,
            highestRank = mapRank(agreements),
            onAgreement = hasAgreement,
            warehouseStock = if (oebsStockResponse != null) mapStock(oebsStockResponse) else emptyList(),
            inStockAnyWarehouse = if (oebsStockResponse != null) inStock(oebsStockResponse) else false
        )
    }


    private fun mapToImageUri(media: List<MediaDoc>): String? {
        return media.minByOrNull { doc -> doc.priority }?.uri
    }

    private fun mapRank(agreements: List<AgreementInfoDoc>): Int =
        agreements.maxOfOrNull { it.rank } ?: 99

    private suspend fun mapStock(oebsStockResponse: List<OebsStockResponse>): List<StockResponse> =
        oebsStockResponse
            .filter { it.organisasjons_navn.substring(4) != "Telemark" }
            .map { stock: OebsStockResponse ->
                StockResponse(
                    stock.organisasjons_navn.substring(4),
                    calculateAvailable(stock)
                )
            }

    private fun calculateAvailable(oebsStockResponse: OebsStockResponse): Int =
        max((oebsStockResponse.tilgjengelig - oebsStockResponse.behovsmeldt), 0)

    private fun inStock(oebsStockResponse: List<OebsStockResponse>): Boolean =
        oebsStockResponse.any { stock -> calculateAvailable(stock) > 0 }

}

@Serdeable
data class AlternativesWithStockNew(val original: ProductResponse, val alternatives: List<ProductResponse?>)

@Serdeable
data class AlternativesWithStockGrouped(val original: ProductResponse, val groups: List<List<ProductResponse>>)

@Language("JSON")
fun searchBodyProduct(hmsNr: String) = """
        {
  "query": {
    "bool": {
      "must": {
        "match": {
          "hmsArtNr": "$hmsNr"
        }
      }
    }
  }
}
        """.trimIndent()

@Serdeable
data class StockResponse(val location: String, val available: Number)

@Serdeable
data class ProductResponse(
    val seriesId: String?,
    val variantId: String,
    val seriesTitle: String,
    val variantTitle: String,
    val status: String,
    val hmsArtNr: String,
    val imageUri: String?,
    val supplierName: String,
    val highestRank: Int,
    val onAgreement: Boolean,
    val warehouseStock: List<StockResponse>,
    val inStockAnyWarehouse: Boolean
)

@Serdeable
data class ProductDoc(
    override val id: String,
    val supplier: ProductSupplier,
    val title: String,
    val articleName: String,
    val attributes: AttributesDoc,
    val status: ProductStatus,
    val hmsArtNr: String? = null,
    val identifier: String,
    val supplierRef: String,
    val seriesId: String? = null,
    val media: List<MediaDoc> = emptyList(),
    val expired: LocalDateTime,
    val hasAgreement: Boolean = false,
    val agreements: List<AgreementInfoDoc> = emptyList(),
) : SearchDoc

@Serdeable
data class AgreementInfoDoc(
    val id: UUID,
    val identifier: String? = null,
    val title: String? = null,
    val label: String,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String? = null,
    val postTitle: String? = null,
    val postId: UUID? = null,
    val refNr: String? = null,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
)

@Serdeable
data class AttributesDoc(
    val manufacturer: String? = null,
    val series: String? = null,
)

@Serdeable
data class MediaDoc(
    val uri: String,
    val priority: Int = 1,
    val type: MediaType = MediaType.IMAGE,
    val text: String? = null,
    val source: MediaSourceType = MediaSourceType.HMDB
)

@Serdeable
data class ProductSupplier(val id: String, val identifier: String, val name: String)
