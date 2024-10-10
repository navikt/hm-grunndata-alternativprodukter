package no.nav.hm.grunndata.alternativprodukter

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
open class AlternativeProductsService(
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
    private val oebsClient: OebsClient,
    private val azureAdClient: AzureAdClient,
    private val azureBody: AzureBody
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductsService::class.java)
    }

    suspend fun getAlternativeProducts(hmsArtnr: String): List<AlternativeProduct> {
        val alternatives = hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtnr).map { it.targetHmsArtnr }
        val authToken = azureAdClient.getToken(azureBody)

        return alternatives.map { AlternativeProduct(it, oebsClient.getWarehouseStock(it, "Bearer ${authToken.access_token}")) }
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

    fun deleteAlternativeProducts(hmsArtnrList: List<String>) {
        require(hmsArtnrList.size >= 2) { "List must contain at least two elements for there to be a mapping" }

        generateMappingList(hmsArtnrList).forEach { (first, second) ->
            hmsArtnrMappingRepository.findBySourceHmsArtnrAndTargetHmsArtnr(first, second)?.let {
                hmsArtnrMappingRepository.deleteMapping(first, second)
            } ?: LOG.warn("Did not find mapping to delete for sourceHmsArtnr: $first and targetHmsArtnr: $second")
        }
    }

    fun saveAlternativeProducts(hmsArtnrList: List<String>) {
        require(hmsArtnrList.size >= 2) { "List must contain at least two elements for there to be a mapping" }

        generateMappingList(hmsArtnrList).forEach { (first, second) ->
            hmsArtnrMappingRepository.findBySourceHmsArtnrAndTargetHmsArtnr(first, second)?.let {
                LOG.warn("Mapping already exists for sourceHmsArtnr: $first and targetHmsArtnr: $second")
            } ?: hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), first, second)
        }
    }
}