package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller("/hmsArtNrMapping")
class HmsArtNrMappingController(private val hmsArtnrMappingRepository: HmsArtnrMappingRepository) {

    @Get("/all/distinct")
    suspend fun getAllDistinctHmsArtnr() = hmsArtnrMappingRepository.findDistinctSourceHmsArtnr()

}