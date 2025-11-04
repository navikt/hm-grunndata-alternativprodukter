package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.serde.annotation.Serdeable
import no.nav.hm.grunndata.alternativprodukter.alternative.AlternativeProductsController.Companion.LOG

@Controller("/hmsArtNrMapping")
class HmsArtNrMappingController(
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
    private val azureAdUserClient: AzureAdUserClient
) {

    @Get("/all/distinct")
    suspend fun getAllDistinctHmsArtnr() = hmsArtnrMappingRepository.findDistinctSourceHmsArtnr()

    @Post("/group/add")
    suspend fun addToGroup(
        @Header("Authorization") authorization: String,
        @Body editGroupDTO: EditGroupDTO
    ): HttpResponse<List<HmsArtnrMappingInputDTO>> {
        val authToken = authorization.removePrefix("Bearer ")

        val tokenValidated = azureAdUserClient.validateToken(AuthBody(token = authToken))

        if (tokenValidated.active) {
            if (editGroupDTO.group.contains(editGroupDTO.alternative)) {
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
        } else {
            LOG.warn("Token fail: " + tokenValidated.error)
            return HttpResponse.unauthorized()
        }
    }

    @Delete("/group/delete")
    suspend fun deleteFromGroup(
        @Header("Authorization") authorization: String,
        @Body editGroupDTO: EditGroupDTO
    ): HttpResponse<String> {
        val tokenValidated = azureAdUserClient.validateToken(AuthBody(token = authorization))

        if (tokenValidated.active) {
            editGroupDTO.group.filter { it !== editGroupDTO.alternative }.forEach {
                hmsArtnrMappingRepository.deleteMapping(
                    sourceHmsArtnr = it, targetHmsArtnr = editGroupDTO.alternative
                )

                hmsArtnrMappingRepository.deleteMapping(
                    sourceHmsArtnr = editGroupDTO.alternative, targetHmsArtnr = it
                )
            }
            return HttpResponse.ok()
        } else {
            LOG.warn("Token fail: " + tokenValidated.error)
            return HttpResponse.unauthorized()
        }
    }
}

@Serdeable
data class EditGroupDTO(val group: List<String>, val alternative: String)