package no.nav.hm.grunndata.alternativprodukter.oebs

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.Serdeable

@Client("\${oebs.url}")
interface OebsClient {

    @Get(value = "/lager/alle-sentraler/{hmsNr}")
    suspend fun getWarehouseStock(hmsNr: String, @Header authorization: String): List<OebsStockResponse>

    @Post(value = "/lager/sentral/enhet/{enhetnr}", consumes = ["application/json"])
    suspend fun getWarehouseStockForCentral(
        enhetnr: String,
        @Body hmsnrs: HmsnrsDTO,
        @Header authorization: String
    ): List<OebsStockResponse>

    @Post(value = "/lager/alle-sentraler", consumes = ["application/json"])
    suspend fun getWarehouseStocks(
        @Body hmsnrs: HmsnrsDTO,
        @Header authorization: String
    ): List<OebsStockResponse>

}

@Serdeable
data class HmsnrsDTO(
    val hmsnrs: Set<String>,
)

@Serdeable
data class OebsStockResponse(
    val antallPåLager: Int,
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
