package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/alternativ")
@Tag(name = "Alternativprodukter")
class AlternativeProductsController(
    private val AlternativeProductsService: AlternativeProductsService,
) {
    @Get("/{hmsArtNr}")
    suspend fun getAlternativeProducts(hmsArtNr: String): HttpResponse<List<String>> {
        return HttpResponse.ok(
            AlternativeProductsService.getAlternativeProducts(hmsArtNr),
        )
    }
}