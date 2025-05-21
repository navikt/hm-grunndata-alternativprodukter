package no.nav.hm.grunndata.alternativprodukter.index

import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client

@Client("\${grunndata.db.url}")
interface TechLabelApiClient {

    @Get(uri = "/api/v1/techlabels", consumes = [APPLICATION_JSON])
    fun fetchAllTechLabel(): Map<String, List<TechLabelDTO>>

}
