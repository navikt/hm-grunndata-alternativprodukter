package no.nav.hm.grunndata.alternativprodukter.index


import jakarta.inject.Singleton
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import org.slf4j.LoggerFactory
import org.opensearch.client.opensearch.OpenSearchClient


@Singleton
class AlternativeProductIndexer(
    private val gdbApiClient: GdbApiClient,
    private val isoCategoryService: IsoCategoryService,
    private val techLabelService: TechLabelService,
    private val alternativProdukterClient: AlternativProdukterClient,
    private val client: OpenSearchClient
) : Indexer(client, settings, mapping, IndexName) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductIndexer::class.java)
        private val settings = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_settings.json")!!.readText()
        private val mapping = AlternativeProductIndexer::class.java
            .getResource("/opensearch/alternative_products_mapping.json")!!.readText()

    }

    fun reIndexAllDinstinctHmsNr() {
        val hmsNrs = alternativProdukterClient.fetchAllDistinctHmsArtnr()
        LOG.info("Reindexing all distinct hmsNr: ${hmsNrs.size}")
        val mappedDoc = mutableListOf<AlternativeProductDoc>()
        hmsNrs.forEach { hmsNr ->
            gdbApiClient.findProductByHmsArtNr(hmsNr)?.let {
                if (it.status != ProductStatus.DELETED) {
                    mappedDoc.add(it.toDoc(isoCategoryService, techLabelService, alternativProdukterClient))
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

    fun reIndexByHmsNr(hmsNr: String) {
        LOG.info("Reindexing hmsNr: $hmsNr")
        gdbApiClient.findProductByHmsArtNr(hmsNr)?.let {
            if (it.status != ProductStatus.DELETED) {
                index(listOf(it.toDoc(isoCategoryService, techLabelService, alternativProdukterClient)))
            }
        }
    }
}

val IndexName = "alternative_products"
