package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import no.nav.hm.grunndata.alternativprodukter.alternative.SearchService
import no.nav.hm.grunndata.alternativprodukter.alternative.SearchService.Companion.ALTERNATIVES
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductDoc
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockDTO
import no.nav.hm.grunndata.alternativprodukter.stock.FetchOebsAndIndexProductStockComponent

@Singleton
class AlternativeQueryResolver(private val searchService: SearchService,
                               private val fetchOebsAndIndexProductStockComponent: FetchOebsAndIndexProductStockComponent,
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

fun buildQueryBody(hmsnrs: List<String>) = """{
  "query": {
    "terms": {
      "alternativeFor": ${hmsnrs.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")}
    }
  }
}"""