package no.nav.hm.grunndata.alternativprodukter

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
open class AlternativeProductsService(
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductsService::class.java)
    }

    fun getAlternativeProducts(hmsArtnr: String): List<String> {
        return hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtnr).map { it.targetHmsArtnr }
    }

    suspend fun saveAlternativeProducts(hmsArtnrList: List<String>) {
        if (hmsArtnrList.size < 2) {
            throw IllegalArgumentException("List must contain at least two elements for there to be a mapping")
        }

        val pairs = mutableListOf<Pair<String, String>>()
        for (i in hmsArtnrList.indices) {
            for (j in hmsArtnrList.indices) {
                if (i != j) {
                    pairs.add(Pair(hmsArtnrList[i], hmsArtnrList[j]))
                }
            }
        }

        pairs.map {
            val existingMapping = hmsArtnrMappingRepository.findBySourceHmsArtnrAndTargetHmsArtnr(it.first, it.second)
            if (existingMapping != null) {
                LOG.warn("Mapping already exists for sourceHmsArtnr: ${it.first} and targetHmsArtnr: ${it.second}")
            } else {
                hmsArtnrMappingRepository.save(HmsArtnrMapping(sourceHmsArtnr = it.first, targetHmsArtnr = it.second))
            }

        }

    }

}
