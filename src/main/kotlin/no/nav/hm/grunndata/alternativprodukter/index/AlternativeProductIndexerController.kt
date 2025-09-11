package no.nav.hm.grunndata.alternativprodukter.index

import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Controller("/internal/index/alternative_products")
@ExecuteOn(TaskExecutors.BLOCKING)
class AlternativeProductIndexerController(private val alternativeProductIndexer: AlternativeProductIndexer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductIndexerController::class.java)
    }

    @Post("/hmsNr/{hmsNr}")
    fun indexAlternativeProductsByHmsNr(hmsNr: String) = runBlocking {
        LOG.info("reIndex alternative products by hmsNr: $hmsNr")
        alternativeProductIndexer.reIndexByHmsNr(hmsNr)
    }

    @Put("/alias/{indexName}")
    fun aliasAlternativeProducts(indexName: String) {
        alternativeProductIndexer.updateAlias(indexName)
    }

    @Get("/alias")
    fun getAlias() = alternativeProductIndexer.getAlias().toJsonString()

    @Get("/count")
    fun count() = alternativeProductIndexer.docCount()

    @Post("/reIndexProductStock")
    fun reIndexProductStock() = runBlocking {
        alternativeProductIndexer.reIndexAllProductStock()
    }

}
