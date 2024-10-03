package no.nav.hm.grunndata.alternativprodukter

import jakarta.inject.Singleton

@Singleton
open class AlternativeProductsService(
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
) {
    open fun getAlternativeProducts(hmsArtnr: String): List<String> {
        return hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtnr).map { it.targetHmsArtnr }
    }
}
