package no.nav.hm.grunndata.alternativprodukter.alternative

import jakarta.inject.Singleton
import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch.generic.Request
import org.opensearch.client.opensearch.generic.Requests
import org.slf4j.LoggerFactory


@Singleton
class SearchService(private val osclient: OpenSearchClient) {

    fun searchWithBody(index: String, params: Map<String, String>, body: String): String {
        val request: Request = newRequest("POST", "/$index/_search", params, body)
        return performRequest(request, params)
    }

    private fun performRequest(request: Request, params: Map<String, String>): String {
        require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return try {
            osclient.generic().execute(request).use { response ->
                response.body.get().bodyAsString()
            }
        }
        catch (e: Exception) {
            LOG.error("Error performing request", e)
            throw e
        }
    }


    private fun newRequest(method: String, endpoint: String, params: Map<String, String>, body: String?): Request {
        val requestBuilder = Requests.builder().method(method).endpoint(endpoint).query(params)
        if (body != null) {
            requestBuilder.json(body)
        }
        return requestBuilder.build()
    }

    private fun onlyAllowedParams(params: Map<String, String>): Boolean {
        return ALLOWED_REQUEST_PARAMS.containsAll(params.keys)
    }

    companion object {
        const val ALTERNATIVES = "alternative_products"
        private val LOG = LoggerFactory.getLogger(SearchService::class.java)
        val ALLOWED_REQUEST_PARAMS = setOf("q", "filter_path", "pretty", "format")
    }

}
