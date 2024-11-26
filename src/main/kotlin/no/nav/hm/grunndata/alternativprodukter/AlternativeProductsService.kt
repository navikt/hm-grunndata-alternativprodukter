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

    suspend fun getAlternativeProducts(hmsArtnr: String): AlternativeProductsResponse {
        val alternatives = hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtnr).map { it.targetHmsArtnr }
        val authToken = azureAdClient.getToken(azureBody)

        return AlternativeProductsResponse(
            ProductStock(hmsArtnr, oebsClient.getWarehouseStock(hmsArtnr, "Bearer ${authToken.access_token}")),
            alternatives.map { ProductStock(it, oebsClient.getWarehouseStock(it, "Bearer ${authToken.access_token}")) }
        )
    }

    suspend fun getAlternativeProductsWithoutStock(hmsArtnr: String): List<String> {
        return hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtnr).map { it.targetHmsArtnr }
    }

    suspend fun getStockAndAlternatives(hmsArtNr: String): ProductStockAlternatives {
        val alternatives = hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtNr).map { it.targetHmsArtnr }
        val authToken = azureAdClient.getToken(azureBody)
        val productStock =  ProductStock(hmsArtNr, oebsClient.getWarehouseStock(hmsArtNr, "Bearer ${authToken.access_token}"))
        return ProductStockAlternatives(productStock, alternatives)
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