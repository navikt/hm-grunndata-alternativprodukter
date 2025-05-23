package no.nav.hm.grunndata.alternativprodukter.index


import jakarta.inject.Singleton
import kotlinx.coroutines.flow.toList
import no.nav.hm.grunndata.alternativprodukter.alternative.AlternativeProductService
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingRepository
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockRepository
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.slf4j.LoggerFactory
import org.opensearch.client.opensearch.OpenSearchClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Singleton
class AlternativeProductIndexer(
    private val gdbApiClient: GdbApiClient,
    private val isoCategoryService: IsoCategoryService,
    private val alternativeProductService: AlternativeProductService,
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
    private val client: OpenSearchClient
) : Indexer(client, settings, mapping, aliasName) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductIndexer::class.java)
        private val settings = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_settings.json")!!.readText()
        private val mapping = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_mapping.json")!!.readText()

    }

    // this fetch from oebs
    suspend fun reIndexAllDinstinctHmsNr() {
        val hmsNrs = hmsArtnrMappingRepository.findDistinctSourceHmsArtnr()
        LOG.info("Reindexing all distinct hmsNr: ${hmsNrs.size}")
        val mappedDoc = mutableListOf<AlternativeProductDoc>()
        hmsNrs.forEach { hmsNr ->
            gdbApiClient.findProductByHmsArtNr(hmsNr)?.let {
                if (it.status != ProductStatus.DELETED) {
                    val iso = isoCategoryService.lookUpCode(it.isoCategory)!!
                    val productStockAlternatives = alternativeProductService.getStockAndAlternativesFromOebs(hmsNr)
                    mappedDoc.add(it.toDoc(iso, productStockAlternatives))
                }
            } ?: LOG.warn("No product found for hmsNr: $hmsNr")
            if (mappedDoc.size > 1000) {
                index(mappedDoc)
                mappedDoc.clear()
            }
        }
        if (mappedDoc.isNotEmpty()) {
            index(mappedDoc)
        }
    }

    // this fetch from our database only
    suspend fun reIndexAllProductStock() {
        val alternativsProductStock = alternativeProductService.getStockAndAlternativesFromDB()
        val newIndexName = "${aliasName}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}"
        createIndex(indexName = newIndexName,settings = settings, mapping = mapping)
        val mappedDoc = mutableListOf<AlternativeProductDoc>()
        alternativsProductStock.forEach { productStock -> gdbApiClient.findProductByHmsArtNr(productStock.original.hmsArtnr)?.let {
                if (it.status != ProductStatus.DELETED) {
                    val iso = isoCategoryService.lookUpCode(it.isoCategory)!!
                    mappedDoc.add(it.toDoc(iso, productStock))
                }
            } ?: LOG.warn("No product found for hmsNr: ${productStock.original.hmsArtnr}")
            if (mappedDoc.size > 1000) {
                index(mappedDoc, newIndexName)
                mappedDoc.clear()
            }
        }
        if (mappedDoc.isNotEmpty()) {
            index(mappedDoc, newIndexName)
        }
    }

    // this fetch from oebs
    suspend fun reIndexByHmsNr(hmsNr: String) {
        LOG.info("Reindexing hmsNr: $hmsNr")
        gdbApiClient.findProductByHmsArtNr(hmsNr)?.let {
            if (it.status != ProductStatus.DELETED) {
                val iso = isoCategoryService.lookUpCode(it.isoCategory)!!
                index(listOf(it.toDoc(iso, alternativeProductService.getStockAndAlternativesFromOebs(hmsNr))))
            }
        }
    }
}

val aliasName = "alternative_products"
