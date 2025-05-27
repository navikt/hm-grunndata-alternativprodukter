package no.nav.hm.grunndata.alternativprodukter.index

import io.micronaut.data.model.Page
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import java.util.UUID
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO

@Client("\${grunndata.db.url}")
interface GdbApiClient {

    @Get(uri = "/api/v1/products/hmsArtNr/{hmsArtNr}", consumes = [APPLICATION_JSON])
    suspend fun findProductByHmsArtNr(hmsArtNr: String): ProductRapidDTO?

    @Get(uri = "/api/v1/isocategories", consumes = [APPLICATION_JSON])
    suspend fun retrieveIsoCategories(): List<IsoCategoryDTO>

}
