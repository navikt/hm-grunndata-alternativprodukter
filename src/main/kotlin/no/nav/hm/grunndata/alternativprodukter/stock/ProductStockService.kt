package no.nav.hm.grunndata.alternativprodukter.stock

import io.micrometer.core.annotation.Timed
import io.micronaut.cache.CacheConfiguration
import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Singleton
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.oebs.AzureAdClient
import no.nav.hm.grunndata.alternativprodukter.oebs.AzureBody
import no.nav.hm.grunndata.alternativprodukter.oebs.HmsnrsDTO
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsClient
import org.slf4j.LoggerFactory

@Singleton
open class ProductStockService(
    private val productStockRepository: ProductStockRepository,
    private val oebsClient: OebsClient,
    private val azureAdClient: AzureAdClient,
    private val azureBody: AzureBody,
    private val micronautCacheConfig: CacheConfiguration
) {

    @Cacheable("product-stock")
    open fun findByHmsArtnr(hmsArtnr: String): ProductStockDTO = runBlocking {
        LOG.info("Finding stock for $hmsArtnr")
        val productStock = productStockRepository.findByHmsArtnr(hmsArtnr)?.let {
            val cacheTime = LocalDateTime.now().minusMinutes(micronautCacheConfig.expireAfterWrite.get().toMinutes())
            if (it.updated.isAfter(cacheTime)) {
                it
            } else null
        } ?: run {
            LOG.info("Fetching from OEBS for $hmsArtnr")
            val authToken = azureAdClient.getToken(azureBody)
            val oebsStockResponse = oebsClient.getWarehouseStock(hmsArtnr, "Bearer ${authToken.access_token}")
            val oebsStock = ProductStock(
                hmsArtnr = hmsArtnr,
                oebsStockResponse = oebsStockResponse
            )
            val saved = productStockRepository.findByHmsArtnr(hmsArtnr)?.let {
                    productStockRepository.update(
                        it.copy(
                            updated = LocalDateTime.now(),
                            oebsStockResponse = oebsStock.oebsStockResponse
                        )
                    )
            } ?: productStockRepository.save(oebsStock)
            saved
        }
        productStock.toDTO()
    }

    @Timed(value = "findByHmsnrsAndEnhet", description = "Time taken to find product stock list of hmsnrs and enhet")
    open fun findByHmsnrsAndEnhet(hmsnrs: Set<String>, enhetnr: String): List<ProductStockDTO> = runBlocking {
        LOG.info("Finding stock for ${hmsnrs} products and enhet $enhetnr")
        val authToken = azureAdClient.getToken(azureBody)
        val oebsStockResponse = oebsClient.getWarehouseStockForCentral(enhetnr = enhetnr, hmsnrs = HmsnrsDTO(hmsnrs = hmsnrs),
            authorization = "Bearer ${authToken.access_token}")
        val productStocks  = oebsStockResponse.map { stock ->
            ProductStock(
                hmsArtnr = stock.artikkelnummer,
                oebsStockResponse = listOf(stock)
            )
        }
        productStocks.map { it.toDTO() }
    }

    @Timed(value = "findByHmsnrs", description = "Time taken to find product stock list of hmsnrs")
    open fun findByHmsnrs(hmsnrs: Set<String>): List<ProductStockDTO> = runBlocking {
        LOG.info("Finding stock for ${hmsnrs} products")
        val authToken = azureAdClient.getToken(azureBody)
        val oebsStockResponse = oebsClient.getWarehouseStocks(hmsnrs = HmsnrsDTO(hmsnrs = hmsnrs),
            authorization = "Bearer ${authToken.access_token}")
        val productStocks  = oebsStockResponse.map { stock ->
            ProductStock(
                hmsArtnr = stock.artikkelnummer,
                oebsStockResponse = listOf(stock)
            )
        }
        productStocks.map { it.toDTO() }
    }


    companion object {
        private val LOG = LoggerFactory.getLogger(ProductStockService::class.java)
    }

}