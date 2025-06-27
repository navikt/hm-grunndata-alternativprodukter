package no.nav.hm.grunndata.alternativprodukter.stock

import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsStockResponse
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsWarehouseService
import org.slf4j.LoggerFactory

@Singleton
open class FetchOebsAndIndexProductStockComponent(
    private val productStockRepository: ProductStockRepository,
    private val oebsWarehouseService: OebsWarehouseService,
    private val indexer: AlternativeProductIndexer
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @Timed("findByHmsArtnr")
    @Counted("findByHmsArtnr.count")
    open fun findByHmsArtnr(hmsArtnr: String): ProductStockDTO = runBlocking {
        LOG.info("Fetching from OEBS for $hmsArtnr")

        val oebsStockResponseList: List<OebsStockResponse> = oebsWarehouseService.getWarehouseStockSingle(hmsArtnr)
        val productStock = ProductStock(
            hmsArtnr = hmsArtnr,
            oebsStockResponse = oebsStockResponseList
        )
        coroutineScope.launch {
            saveAndReindex(listOf(productStock))
        }
        productStock.toDTO()
    }

    @Timed("findByHmsnrsAndEnhet")
    @Counted("findByHmsnrsAndEnhet.count")
    open fun findByHmsnrsAndEnhet(hmsnrs: Set<String>, enhetnr: String): List<ProductStockDTO> = runBlocking {
        LOG.info("Fetching from OEBS for ${hmsnrs} products and enhet $enhetnr")
        val oebsStockResponseList = oebsWarehouseService.getWarehouseStockForCentral(
            enhetnr = enhetnr, hmsnrs = hmsnrs
        )
        val hmsnrStockGroup = oebsStockResponseList.groupBy { it.artikkelnummer }
        val productStocks = hmsnrStockGroup.map { group ->
            ProductStock(
                hmsArtnr = group.key,
                oebsStockResponse = group.value
            )
        }
        validateProductStocks(productStocks, hmsnrs)
        productStocks.map { it.toDTO() }
    }

    private fun validateProductStocks(productStocks: List<ProductStock>, hmsnrs: Set<String>) {
        if (productStocks.isEmpty()) {
            LOG.warn("No product stocks found for $hmsnrs")
        } else if (productStocks.size != hmsnrs.size) {
            LOG.warn("Found ${productStocks.size} product stocks for the given HMS numbers ${hmsnrs.size}")
        } else if (productStocks.size > 20) {
            LOG.warn("Found more than 20 this might cause a performance issue")
        }
        productStocks.forEach { stock ->
            if (stock.hmsArtnr !in hmsnrs) {
                throw Throwable("Product stock with HMS number ${stock.hmsArtnr} not found in the provided HMS numbers: $hmsnrs")
            }
        }
    }

    @Timed("findByHmsnrs")
    @Counted("findByHmsnrs.count")
    open fun findByHmsnrs(hmsnrs: Set<String>): List<ProductStockDTO> = runBlocking {
        LOG.info("Fetching from OEBS for ${hmsnrs} products")
        val oebsStockResponseList = oebsWarehouseService.getWarehouseStocks(
            hmsnrs = hmsnrs
        )
        val hmsnrStockGroup = oebsStockResponseList.groupBy { it.artikkelnummer }
        val productStocks = hmsnrStockGroup.map { group ->
            ProductStock(
                hmsArtnr = group.key,
                oebsStockResponse = group.value
            )
        }
        validateProductStocks(productStocks, hmsnrs)
        coroutineScope.launch {
            saveAndReindex(productStocks)
        }
        productStocks.map { it.toDTO() }
    }

    suspend fun saveAndReindex(oebsStocks: List<ProductStock>) {
        LOG.info("Saving and reindexing ${oebsStocks.size} product stocks")
        val hmsnrs = oebsStocks.map { oebsStock ->
            val saved = productStockRepository.findByHmsArtnr(oebsStock.hmsArtnr)?.let {
                productStockRepository.update(
                    it.copy(
                        updated = LocalDateTime.now(),
                        oebsStockResponse = oebsStock.oebsStockResponse
                    )
                )
            } ?: productStockRepository.save(oebsStock)
            oebsStock.hmsArtnr
        }
        indexer.reIndexByHmsNrs(hmsnrs.toSet())
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(FetchOebsAndIndexProductStockComponent::class.java)
    }

}