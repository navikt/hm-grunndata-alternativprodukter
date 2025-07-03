package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMapping
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingInputDTO
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingRepository
import java.util.UUID


@Singleton
class HmsArtnrMappingResolver(
    private val repository: HmsArtnrMappingRepository
) {


    fun getHmsArtnrMappingById(id: UUID): HmsArtnrMapping? = runBlocking  {
        repository.findById(id)
    }

    fun getHmsArtnrMappingBySourceHmsArtnr(sourceHmsArtnr: String) = runBlocking {
        repository.findBySourceHmsArtnr(sourceHmsArtnr)
    }

    fun createHmsArtnrMapping(input: HmsArtnrMappingInputDTO): List<HmsArtnrMapping> = runBlocking {
        val firstMapping = repository.findBySourceHmsArtnrAndTargetHmsArtnr(sourceHmsArtnr = input.sourceHmsArtnr, targetHmsArtnr = input.targetHmsArtnr) ?: repository.save(
            HmsArtnrMapping(
                sourceHmsArtnr = input.sourceHmsArtnr,
                targetHmsArtnr = input.targetHmsArtnr
            )
        )
        // create a reverse mapping as well
        val secondMapping = repository.findBySourceHmsArtnrAndTargetHmsArtnr(sourceHmsArtnr = input.targetHmsArtnr, targetHmsArtnr = input.sourceHmsArtnr) ?: repository.save(
            HmsArtnrMapping(
                sourceHmsArtnr = input.targetHmsArtnr,
                targetHmsArtnr = input.sourceHmsArtnr
            )
        )
        listOf(firstMapping, secondMapping)
    }

    fun updateHmsArtnrMapping(id: UUID, input: HmsArtnrMappingInputDTO): HmsArtnrMapping? = runBlocking {
        repository.findById(id)?.let {
            repository.update(it.copy(sourceHmsArtnr = input.sourceHmsArtnr, targetHmsArtnr = input.targetHmsArtnr))
        }
    }


    fun deleteHmsArtnrMapping(input: HmsArtnrMappingInputDTO): Boolean = runBlocking{
        repository.deleteMapping(sourceHmsArtnr = input.sourceHmsArtnr, targetHmsArtnr = input.targetHmsArtnr)
        repository.deleteMapping(sourceHmsArtnr = input.targetHmsArtnr, targetHmsArtnr = input.sourceHmsArtnr)
            true
    }


}