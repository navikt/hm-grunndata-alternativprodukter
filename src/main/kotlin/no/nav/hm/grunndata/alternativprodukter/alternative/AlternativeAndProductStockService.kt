package no.nav.hm.grunndata.alternativprodukter.alternative

import jakarta.inject.Singleton
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockRepository
import no.nav.hm.grunndata.alternativprodukter.stock.toDTO
import org.slf4j.LoggerFactory

@Singleton
open class AlternativeAndProductStockService (
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
    private val productStockRepository: ProductStockRepository

) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductService::class.java)
    }

    suspend fun getStockAndAlternativesFromDB(): List<ProductStockAlternatives>  {
        val productStock = productStockRepository.findAll().map { it.toDTO() }.toList()
        return productStock.map { productStock ->
            val alternatives = hmsArtnrMappingRepository.findBySourceHmsArtnr(productStock.hmsArtNr).map { it.targetHmsArtnr }
            ProductStockAlternatives(productStock, alternatives)
        }
    }

    suspend fun getStockAndAlternativesFromDB(hmsArtNr: String): ProductStockAlternatives? {
        productStockRepository.findByHmsArtnr(hmsArtNr)?.let { productStock ->
            val alternatives = hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtNr).map { it.targetHmsArtnr }
            return ProductStockAlternatives(productStock.toDTO(), alternatives)
        }
        return null
    }




}