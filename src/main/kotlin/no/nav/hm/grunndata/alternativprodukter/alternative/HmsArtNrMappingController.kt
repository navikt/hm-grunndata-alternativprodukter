package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.serde.annotation.Serdeable

@Controller("/hmsArtNrMapping")
class HmsArtNrMappingController(private val hmsArtnrMappingRepository: HmsArtnrMappingRepository) {

    @Get("/all/distinct")
    suspend fun getAllDistinctHmsArtnr() = hmsArtnrMappingRepository.findDistinctSourceHmsArtnr()

    @Post("/create")
    suspend fun createMapping(
        @Body mapping: HmsArtnrMappingInputDTO
    ): List<HmsArtnrMappingInputDTO> {
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

        return listOf(createdMapping.toDto(), createdReverseMapping.toDto())
    }

    @Post("/group/add")
    suspend fun addToGroup(
        @Body editGroupDTO: EditGroupDTO
    ): HttpResponse<List<HmsArtnrMappingInputDTO>> {

        if(editGroupDTO.group.contains(editGroupDTO.alternative)) {
            return HttpResponse.badRequest()
        }

        val newMappings = editGroupDTO.group.map {
            val createdMapping = hmsArtnrMappingRepository.save(
                HmsArtnrMapping(
                    sourceHmsArtnr = it,
                    targetHmsArtnr = editGroupDTO.alternative
                )
            )

            val createdReverseMapping = hmsArtnrMappingRepository.save(
                HmsArtnrMapping(
                    sourceHmsArtnr = editGroupDTO.alternative,
                    targetHmsArtnr = it
                )
            )
            listOf(createdMapping.toDto(), createdReverseMapping.toDto())
        }.flatten()

        return HttpResponse.ok(newMappings)
    }

    @Delete("/group/delete")
    suspend fun deleteFromGroup(
        @Body editGroupDTO: EditGroupDTO
    ) {
        editGroupDTO.group.filter { it !== editGroupDTO.alternative }.forEach {
            hmsArtnrMappingRepository.deleteMapping(
                sourceHmsArtnr = it, targetHmsArtnr = editGroupDTO.alternative
            )

            hmsArtnrMappingRepository.deleteMapping(
                sourceHmsArtnr = editGroupDTO.alternative, targetHmsArtnr = it
            )
        }
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

@Serdeable
data class EditGroupDTO(val group: List<String>, val alternative: String)