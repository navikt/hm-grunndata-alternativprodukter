package no.nav.hm.grunndata.alternativprodukter.index


import jakarta.inject.Singleton
import no.nav.hm.grunndata.alternativprodukter.alternative.AlternativeAndProductStockService
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingRepository
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.slf4j.LoggerFactory
import org.opensearch.client.opensearch.OpenSearchClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Singleton
class AlternativeProductIndexer(
    private val gdbApiClient: GdbApiClient,
    private val isoCategoryService: IsoCategoryService,
    private val alternativeAndProductStockService: AlternativeAndProductStockService,
    private val client: OpenSearchClient
) : Indexer(client, settings, mapping, aliasName) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductIndexer::class.java)
        private val settings = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_settings.json")!!.readText()
        private val mapping = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_mapping.json")!!.readText()

    }



    // this fetch from our database only
    suspend fun reIndexAllProductStock() {
        val alternativsProductStock = alternativeAndProductStockService.getStockAndAlternativesFromDB()
        val newIndexName = "${aliasName}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))}"
        createIndex(indexName = newIndexName, settings = settings, mapping = mapping)
        val mappedDoc = mutableListOf<AlternativeProductDoc>()
        alternativsProductStock.forEach { productStock ->
            gdbApiClient.findProductByHmsArtNr(productStock.original.hmsArtNr)?.let {
                if (it.status != ProductStatus.DELETED) {
                    val iso = isoCategoryService.lookUpCode(it.isoCategory)!!
                    mappedDoc.add(it.toDoc(iso, productStock))
                }
            } ?: LOG.warn("No product found for hmsNr: ${productStock.original.hmsArtNr}")
            if (mappedDoc.size > 1000) {
                index(mappedDoc, newIndexName)
                mappedDoc.clear()
            }
        }
        if (mappedDoc.isNotEmpty()) {
            index(mappedDoc, newIndexName)
        }
    }

    // this fetch from database
    suspend fun reIndexByHmsNr(hmsNr: String) {
        LOG.info("Reindexing hmsNr: $hmsNr")
        gdbApiClient.findProductByHmsArtNr(hmsNr)?.let {
            if (it.status != ProductStatus.DELETED) {
                val iso = isoCategoryService.lookUpCode(it.isoCategory)!!
                alternativeAndProductStockService.getStockAndAlternativesFromDB(hmsNr)?.let { alternatives ->
                    val response = index(listOf(it.toDoc(iso, alternatives)))
                    LOG.info("Got response from indexing: ${response.errors()}")
                }
            }
        }
    }

    suspend fun reIndexByHmsNrs(hmsNrs: Set<String>) {
        LOG.info("Reindexing hmsNrs: $hmsNrs")
        val mappedDoc = mutableListOf<AlternativeProductDoc>()
        hmsNrs.forEach { hmsNr ->
            gdbApiClient.findProductByHmsArtNr(hmsNr)?.let {
                if (it.status != ProductStatus.DELETED) {
                    val iso = isoCategoryService.lookUpCode(it.isoCategory)!!
                    alternativeAndProductStockService.getStockAndAlternativesFromDB(hmsNr)?.let { alternatives ->
                        mappedDoc.add(it.toDoc(iso, alternatives))
                    }
                }
            } ?: LOG.warn("No product found for hmsNr: $hmsNr")
        }
        if (mappedDoc.isNotEmpty()) {
            //val response = index(mappedDoc)
            //LOG.info("Got response from indexing: ${response.errors()}")
        }
    }
}

val aliasName = "alternative_products"
