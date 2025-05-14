package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.cache.CacheConfiguration
import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Singleton
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.oebs.AzureAdClient
import no.nav.hm.grunndata.alternativprodukter.oebs.AzureBody
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
            val oebsStock = ProductStock(
                hmsArtnr = hmsArtnr,
                oebsStockResponse = oebsClient.getWarehouseStock(hmsArtnr, "Bearer ${authToken.access_token}")
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


    companion object {
        private val LOG = LoggerFactory.getLogger(ProductStockService::class.java)
    }

}