package no.nav.hm.grunndata.alternativprodukter.index

import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Controller("/internal/index/alternative_products")
class AlternativeProductIndexerController(private val alternativeProductIndexer: AlternativeProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductIndexerController::class.java)
    }

    @Post("/hmsNr/{hmsNr}")
    suspend fun indexAlternativeProductsByHmsNr(hmsNr: String) {
        LOG.info("reIndex alternative products by hmsNr: $hmsNr")
        alternativeProductIndexer.reIndexByHmsNr(hmsNr)
    }

    @Put("/alias/{indexName}")
    suspend fun aliasAlternativeProducts(indexName: String) {
        alternativeProductIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    suspend fun getAlias() = alternativeProductIndexer.getAlias().toJsonString()

    @Get("/count")
    suspend fun count() = alternativeProductIndexer.docCount()

    @Post("/reIndexProductStock")
    suspend fun reIndexProductStock() {
        alternativeProductIndexer.reIndexAllProductStock()
    }

}
