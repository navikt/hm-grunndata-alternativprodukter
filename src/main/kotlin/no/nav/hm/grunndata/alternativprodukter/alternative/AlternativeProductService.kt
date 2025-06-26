package no.nav.hm.grunndata.alternativprodukter.alternative

import jakarta.inject.Singleton
import java.util.UUID
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory

@Singleton
open class AlternativeProductService(
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductService::class.java)
    }


    suspend fun getAlternativeProductsWithoutStock(hmsArtnr: String): List<String> {
        return hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtnr).map { it.targetHmsArtnr }
    }


    fun getAlternativeMappings() = flow<HmsArtnrMappingInputDTO> {
        hmsArtnrMappingRepository.findAll().collect {
            mapping -> emit(mapping.toDto())
        }
    }

    private fun generateMappingList(hmsArtnrList: List<String>): List<Pair<String, String>> =
        hmsArtnrList.indices.flatMap { i ->
            hmsArtnrList.indices.mapNotNull { j ->
                if (i != j) Pair(
                    hmsArtnrList[i],
                    hmsArtnrList[j]
                ) else null
            }
        }

    suspend fun deleteAlternativeProducts(hmsArtnrList: List<String>) {
        require(hmsArtnrList.size >= 2) { "List must contain at least two elements for there to be a mapping" }

        generateMappingList(hmsArtnrList).forEach { (first, second) ->
            hmsArtnrMappingRepository.findBySourceHmsArtnrAndTargetHmsArtnr(first, second)?.let {
                hmsArtnrMappingRepository.deleteMapping(first, second)
            } ?: LOG.warn("Did not find mapping to delete for sourceHmsArtnr: $first and targetHmsArtnr: $second")
        }
    }

    suspend fun saveAlternativeProducts(hmsArtnrList: List<String>) {
        if(hmsArtnrList.size == 1) {
            LOG.warn("List must contain at least two elements for there to be a mapping")
            return
        }

        generateMappingList(hmsArtnrList).forEach { (first, second) ->
            hmsArtnrMappingRepository.findBySourceHmsArtnrAndTargetHmsArtnr(first, second)?.let {
                LOG.warn("Mapping already exists for sourceHmsArtnr: $first and targetHmsArtnr: $second")
            } ?: hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), first, second)
        }
    }

}