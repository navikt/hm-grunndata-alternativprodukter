package no.nav.hm.grunndata.alternativprodukter.stock

import io.micrometer.core.annotation.Timed

import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductIndexer
import no.nav.hm.grunndata.alternativprodukter.oebs.AzureAdClient
import no.nav.hm.grunndata.alternativprodukter.oebs.AzureBody
import no.nav.hm.grunndata.alternativprodukter.oebs.HmsnrsDTO
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsClient
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsStockResponse
import org.slf4j.LoggerFactory

@Singleton
open class FetchOebsAndIndexProductStockComponent(
    private val productStockRepository: ProductStockRepository,
    private val oebsClient: OebsClient,
    private val azureAdClient: AzureAdClient,
    private val azureBody: AzureBody,
    private val indexer: AlternativeProductIndexer
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @Timed(value = "findByHmsnr", description = "Time taken to find product stock hmsnr")
    open fun findByHmsArtnr(hmsArtnr: String): ProductStockDTO = runBlocking {
        LOG.info("Fetching from OEBS for $hmsArtnr")
        val authToken = azureAdClient.getToken(azureBody)
        val oebsStockResponseList: List<OebsStockResponse> = oebsClient.getWarehouseStock(hmsArtnr, "Bearer ${authToken.access_token}")
        val productStock = ProductStock(
            hmsArtnr = hmsArtnr,
            oebsStockResponse = oebsStockResponseList
        )
        coroutineScope.launch {
            saveAndReindex(listOf(productStock))
        }
        productStock.toDTO()
    }

    @Timed(value = "findByHmsnrsAndEnhet", description = "Time taken to find product stock list of hmsnrs and enhet")
    open fun findByHmsnrsAndEnhet(hmsnrs: Set<String>, enhetnr: String): List<ProductStockDTO> = runBlocking {
        LOG.info("Fetching from OEBS for ${hmsnrs} products and enhet $enhetnr")
        val authToken = azureAdClient.getToken(azureBody)
        val oebsStockResponseList = oebsClient.getWarehouseStockForCentral(
            enhetnr = enhetnr, hmsnrs = HmsnrsDTO(hmsnrs = hmsnrs),
            authorization = "Bearer ${authToken.access_token}"
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
        } else {
            LOG.info("Found ${productStocks.size} product stocks for the given HMS numbers")
        }
        productStocks.forEach { stock ->
           if (stock.hmsArtnr !in hmsnrs) {
                throw Throwable("Product stock with HMS number ${stock.hmsArtnr} not found in the provided HMS numbers: $hmsnrs")
            }
        }
    }

    @Timed(value = "findByHmsnrs", description = "Time taken to find product stock list of hmsnrs")
    open fun findByHmsnrs(hmsnrs: Set<String>): List<ProductStockDTO> = runBlocking {
        LOG.info("Fetching from OEBS for ${hmsnrs} products")
        val authToken = azureAdClient.getToken(azureBody)
        val oebsStockResponseList = oebsClient.getWarehouseStocks(
            hmsnrs = HmsnrsDTO(hmsnrs = hmsnrs),
            authorization = "Bearer ${authToken.access_token}"
        )
        val hmsnrStockGroup = oebsStockResponseList.groupBy { it.artikkelnummer }
        val productStocks = hmsnrStockGroup.map { group ->
            ProductStock(
                hmsArtnr = group.key,
                oebsStockResponse = group.value
            )
        }

        coroutineScope.launch {
            saveAndReindex(productStocks)
        }
        productStocks.map { it.toDTO() }
    }

    @Transactional
    open fun saveAndReindex(oebsStocks: List<ProductStock>) = runBlocking {
        LOG.info("Saving and reindexing ${oebsStocks.size} product stocks")
        val hmsnrs = oebsStocks.map  { oebsStock ->
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