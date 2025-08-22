package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post

@Controller("/hmsArtNrMapping")
class HmsArtNrMappingController(private val hmsArtnrMappingRepository: HmsArtnrMappingRepository) {

    @Get("/all/distinct")
    suspend fun getAllDistinctHmsArtnr() = hmsArtnrMappingRepository.findDistinctSourceHmsArtnr()

    @Post("/create")
    suspend fun createMapping(
        @Body mapping: HmsArtnrMappingInputDTO
    ): List<HmsArtnrMapping> {
        val createdMapping = hmsArtnrMappingRepository.save(
            HmsArtnrMapping(
                sourceHmsArtnr = mapping.sourceHmsArtnr,
                targetHmsArtnr = mapping.targetHmsArtnr
            )
        )

        val createdReverseMapping = hmsArtnrMappingRepository.save(
            HmsArtnrMapping(
                sourceHmsArtnr = mapping.targetHmsArtnr,
                targetHmsArtnr = mapping.sourceHmsArtnr
            )
        )

        return listOf(createdMapping, createdReverseMapping)
    }

    @Delete("/delete")
    suspend fun deleteMapping(
        @Body mapping: HmsArtnrMappingInputDTO
    ) {
        hmsArtnrMappingRepository.deleteMapping(
            sourceHmsArtnr = mapping.sourceHmsArtnr,
            targetHmsArtnr = mapping.targetHmsArtnr
        )

        hmsArtnrMappingRepository.deleteMapping(
            sourceHmsArtnr = mapping.targetHmsArtnr,
            targetHmsArtnr = mapping.sourceHmsArtnr
        )
    }


}