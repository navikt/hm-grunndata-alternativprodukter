package no.nav.hm.grunndata.alternativprodukter.alternativ

import jakarta.inject.Singleton
import java.util.UUID
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockService
import org.slf4j.LoggerFactory

@Singleton
open class AlternativeProductsService(
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
    private val productStockService: ProductStockService
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductsService::class.java)
    }


    suspend fun getAlternativeProductsWithoutStock(hmsArtnr: String): List<String> {
        return hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtnr).map { it.targetHmsArtnr }
    }

    fun getStockAndAlternatives(hmsArtNr: String): ProductStockAlternatives = runBlocking {
        val alternatives = hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtNr).map { it.targetHmsArtnr }
        val productStock = productStockService.findByHmsArtnr(hmsArtNr)
        ProductStockAlternatives(productStock, alternatives)
    }

    fun getAlternativeMappings() = flow<HmsArtnrMappingDto> {
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