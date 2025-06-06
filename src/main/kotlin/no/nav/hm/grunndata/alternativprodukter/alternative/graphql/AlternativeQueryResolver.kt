package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nav.hm.grunndata.alternativprodukter.alternative.SearchService
import no.nav.hm.grunndata.alternativprodukter.alternative.SearchService.Companion.ALTERNATIVES
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductDoc
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockDTO
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockService

@Singleton
class AlternativeQueryResolver(private val searchService: SearchService,
                               private val productStockService: ProductStockService,
                               private val indexer: AlternativeProductIndexer,
                               private val objectMapper: ObjectMapper) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun searchAlternativeProducts(hmsNrs: List<String>): List<AlternativeProductDoc> {
        val body = buildQueryBody(hmsNrs)
        LOG.debug("Query: $body")
        val response = searchService.searchWithBody(index = ALTERNATIVES, params = emptyMap(), body = body )
        val json =  objectMapper.readTree(response)
        val hits = json.get("hits")?.get("hits")
        if (hits!=null) {
            return hits.map { objectMapper.treeToValue(it.get("_source"), AlternativeProductDoc::class.java) }
        }
        LOG.info("No hits found for $hmsNrs")
        return emptyList()
    }

    fun getProductStock(hmsnr: String): ProductStockDTO {
        LOG.debug("Getting stock for $hmsnr")
        val productStockDTO = productStockService.findByHmsArtnr(hmsnr)
        coroutineScope.launch {
            indexer.reIndexByHmsNr(hmsnr)
        }
        return productStockDTO
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(AlternativeQueryResolver::class.java)
    }
}

fun buildQueryBody(hmsnrs: List<String>) = """{
  "query": {
    "terms": {
      "alternativeFor": ${hmsnrs.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")}
    }
  }
}"""