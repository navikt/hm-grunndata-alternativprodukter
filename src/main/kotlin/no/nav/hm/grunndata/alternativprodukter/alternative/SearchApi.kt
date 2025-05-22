package no.nav.hm.grunndata.alternativprodukter.alternative

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.http.HttpHeaders.CACHE_CONTROL
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime

@Controller
class SearchApi(
    private val searchService: SearchService,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SearchApi::class.java)
    }

    @Post(uris = ["/alternative_products/_search{?params*}"])
    fun searchAlternativesWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for alternatives")
        return HttpResponse.ok(searchService.searchWithBody(SearchService.Companion.ALTERNATIVES, params?: emptyMap(), body))
    }


}
