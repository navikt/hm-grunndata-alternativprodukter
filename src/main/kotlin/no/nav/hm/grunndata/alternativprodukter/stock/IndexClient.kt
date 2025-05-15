package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${grunndata.index.url}")
interface IndexClient {

    @Post(uri = "/internal/index/alternative_products/hmsNr/{hmsNr}", consumes = ["application/json"])
    suspend fun indexAlternativeProductsByHmsNr(hmsNr: String)
}