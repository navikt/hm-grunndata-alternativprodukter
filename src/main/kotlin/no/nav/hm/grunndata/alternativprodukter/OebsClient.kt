package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Client("\${oebs.url}")
interface OebsClient {
    @Get(value = "/lager/alle-sentraler/{hmsNr}")
    suspend fun getWarehouseStock(hmsNr: String, @Header authorization: String): List<WarehouseStockResponse>
}

@Singleton
@CacheConfig("warehouse-stock")
open class OebsService(
    private val oebsClient: OebsClient,
    private val azureAdClient: AzureAdClient,
    private val azureBody: AzureBody
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(OebsService::class.java)
    }

    @Cacheable
    open fun getWarehouseStock(hmsNr: String): List<WarehouseStockResponse> = runBlocking {
        LOG.debug("Getting warehouse stock for hmsNr: $hmsNr")
        val authToken = azureAdClient.getToken(azureBody)
        val authorization = "Bearer ${authToken.access_token}"
        oebsClient.getWarehouseStock(hmsNr, authorization)
    }
}

@Serdeable
data class WarehouseStockResponse(
    val erPåLager: Boolean,

    val organisasjons_id: Int,
    val organisasjons_navn: String,
    val artikkelnummer: String,
    val artikkelid: Int,
    val fysisk: Int,
    val tilgjengeligatt: Int,
    val tilgjengeligroo: Int,
    val tilgjengelig: Int,
    val behovsmeldt: Int,
    val reservert: Int,
    val restordre: Int,
    val bestillinger: Int,
    val anmodning: Int,
    val intanmodning: Int,
    val forsyning: Int,
    val sortiment: Boolean,
    val lagervare: Boolean,
    val minmax: Boolean,
)
