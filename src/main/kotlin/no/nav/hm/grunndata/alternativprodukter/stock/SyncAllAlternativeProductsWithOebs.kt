package no.nav.hm.grunndata.alternativprodukter.stock

import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingRepository
import no.nav.hm.grunndata.alternativprodukter.alternative.ProductStockAlternatives
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductDoc
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer
import no.nav.hm.grunndata.alternativprodukter.index.GdbApiClient
import no.nav.hm.grunndata.alternativprodukter.index.IsoCategoryService
import no.nav.hm.grunndata.alternativprodukter.index.toDoc
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsWarehouseService
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import java.time.LocalDateTime

@Singleton
class SyncAllAlternativeProductsWithOebs(private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
                                         private val gdbApiClient: GdbApiClient,
                                         private val isoCategoryService: IsoCategoryService,
                                         private val productStockRepository: ProductStockRepository,
                                         private val oebsWarehouseService: OebsWarehouseService,
                                         private val indexer: AlternativeProductIndexer){


    suspend fun reIndexAllDinstinctHmsNr() {
        val hmsNrs = hmsArtnrMappingRepository.findDistinctSourceHmsArtnr()
        LOG.info("Reindexing all distinct hmsNr: ${hmsNrs.size}")
        val mappedDoc = mutableListOf<AlternativeProductDoc>()
        hmsNrs.forEach { hmsNr ->
            gdbApiClient.findProductByHmsArtNr(hmsNr)?.let {
                if (it.status != ProductStatus.DELETED) {
                    val iso = isoCategoryService.lookUpCode(it.isoCategory)!!
                    val productStockAlternatives = getStockAndAlternativesFromOebs(hmsNr)
                    mappedDoc.add(it.toDoc(iso, productStockAlternatives))
                }
            } ?: LOG.warn("No product found for hmsNr: $hmsNr")
            if (mappedDoc.size > 1000) {
                indexer.index(mappedDoc)
                mappedDoc.clear()
            }
        }
        if (mappedDoc.isNotEmpty()) {
            indexer.index(mappedDoc)
        }
        LOG.info("Reindexing all distinct hmsNr finished")
    }

    fun getStockAndAlternativesFromOebs(hmsArtnr: String): ProductStockAlternatives = runBlocking {
        val alternatives = hmsArtnrMappingRepository.findBySourceHmsArtnr(hmsArtnr).map { it.targetHmsArtnr }
        val oebsStockResponse = oebsWarehouseService.getWarehouseStockSingle(hmsArtnr)
        if (oebsStockResponse.isEmpty()) {
            LOG.warn("No stock found for hmsArtnr: $hmsArtnr")
        }
        val productStock = ProductStock(
            hmsArtnr = hmsArtnr,
            oebsStockResponse = oebsStockResponse
        )
        val saved = productStockRepository.findByHmsArtnr(hmsArtnr)?.let {
            productStockRepository.update(it.copy(
                status = productStock.status,
                oebsStockResponse = oebsStockResponse,
                updated = LocalDateTime.now()))
        } ?: productStockRepository.save(productStock)
        LOG.info("Saved product stock for hmsArtnr: ${saved.hmsArtnr} with updated timestamp: ${saved.updated}")
        ProductStockAlternatives(saved.toDTO(), alternatives)
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(SyncAllAlternativeProductsWithOebs::class.java)
    }

}