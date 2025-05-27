package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory

@Controller
class SearchApiController (
    private val searchService: SearchService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(SearchApiController::class.java)
    }

    @Post(uris = ["/alternative_products/_search{?params*}"])
    fun searchAlternativesWithBody(@QueryValue params: Map<String, String>?, @Body body: String): HttpResponse<String> {
        LOG.info("Got body request for alternatives")
        val response = searchService.searchWithBody(SearchService.Companion.ALTERNATIVES, params?: emptyMap(), body)
        return HttpResponse.ok(response)
    }



}
