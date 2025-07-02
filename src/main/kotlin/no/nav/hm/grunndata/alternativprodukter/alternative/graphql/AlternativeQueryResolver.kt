package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import no.nav.hm.grunndata.alternativprodukter.alternative.SearchService
import no.nav.hm.grunndata.alternativprodukter.alternative.SearchService.Companion.ALTERNATIVES
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductDoc
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer
import no.nav.hm.grunndata.alternativprodukter.index.WareHouseStockDoc
import no.nav.hm.grunndata.alternativprodukter.index.toDoc
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockDTO
import no.nav.hm.grunndata.alternativprodukter.stock.FetchOebsAndIndexProductStockComponent

@Singleton
class AlternativeQueryResolver(
    private val searchService: SearchService,
    private val fetchOebsAndIndexProductStockComponent: FetchOebsAndIndexProductStockComponent,
    private val objectMapper: ObjectMapper
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val refreshStockHitsLimit: Int = 10;

    fun searchAlternativeProducts(hmsNrs: List<String>): List<AlternativeProductDoc> {
        val json = objectMapper.readTree(searchOpenSearch(hmsNrs, 0, 1000))
        val hits = json.get("hits")?.get("hits")
        if (hits != null) {
            return hits.map { objectMapper.treeToValue(it.get("_source"), AlternativeProductDoc::class.java) }
        }
        return emptyList()
    }

    private fun searchOpenSearch(hmsNrs: List<String>, from: Int, size: Int): String {
        val body = buildQueryBody(hmsNrs, from, size)
        LOG.debug("Query: $body")
        return searchService.searchWithBody(index = ALTERNATIVES, params = emptyMap(), body = body)
    }


    fun searchAlternativeProductsPage(hmsNrs: List<String>, from: Int, size: Int): AlternativeProductsPage {
        val json = objectMapper.readTree(searchOpenSearch(hmsNrs, from, size))
        val totalHits = json.get("hits")?.get("total")?.get("value")?.asInt() ?: 0
        val hits = json.get("hits")?.get("hits")
        val alternatives =
            if (hits != null) hits.map {
                objectMapper.treeToValue(
                    it.get("_source"),
                    AlternativeProductDoc::class.java
                )
            }
            else emptyList<AlternativeProductDoc>()
        LOG.debug("alterternative size: ${alternatives.size}, total hits: $totalHits, from: $from, size: $size")
        if (alternatives.isNotEmpty() && alternatives.size <= refreshStockHitsLimit) {
            LOG.info("Refreshing stock for ${alternatives.size} alternative products")
            val hmsnrs = alternatives.map { it.hmsArtNr!! }.toSet()
            val productStocks = fetchOebsAndIndexProductStockComponent.findByHmsnrs(hmsnrs).groupBy { it.hmsArtNr }
            val refreshedAltneratives = alternatives.map { alternative ->
                val stock = productStocks[alternative.hmsArtNr]
                alternative.copy(
                    wareHouseStock = stock!!.flatMap { productStockDTO ->
                        productStockDTO.warehouseStock.map {
                            it.toDoc(
                                productStockDTO
                            )
                        }
                    }
                )
            }

            return AlternativeProductsPage(
                content = refreshedAltneratives,
                total = totalHits,
                from = from,
                size = size
            )

        } else {
            LOG.info("Skipping stock refresh for ${alternatives.size} alternative products, size is above limit $refreshStockHitsLimit")
        }
        return AlternativeProductsPage(
            content = alternatives,
            total = totalHits,
            from = from,
            size = size
        )

    }


    fun getProductStock(hmsnr: String): ProductStockDTO {
        LOG.debug("Getting stock for $hmsnr")
        val productStockDTO = fetchOebsAndIndexProductStockComponent.findByHmsArtnr(hmsnr)
        return productStockDTO
    }

    fun getProductStocksByEnhetNr(hmsnrs: Set<String>, enhetnr: String): List<ProductStockDTO> {
        LOG.info("Getting stocks for $hmsnrs and enhet $enhetnr")
        return fetchOebsAndIndexProductStockComponent.findByHmsnrsAndEnhet(hmsnrs, enhetnr)
    }

    fun getProductStocks(hmsnrs: Set<String>): List<ProductStockDTO> {
        LOG.info("Getting stocks for $hmsnrs")
        return fetchOebsAndIndexProductStockComponent.findByHmsnrs(hmsnrs)
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(AlternativeQueryResolver::class.java)
    }
}


@Serdeable
data class AlternativeProductsPage(
    val content: List<AlternativeProductDoc>,
    val total: Int,
    val from: Int,
    val size: Int
)

fun buildQueryBody(hmsnrs: List<String>, from: Int = 0, size: Int = 1000) = """
{
  "from": $from,
  "size": $size,
  "query": {
    "terms": {
      "alternativeFor": ${hmsnrs.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")}
    }
  }
}
"""