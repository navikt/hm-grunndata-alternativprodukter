package no.nav.hm.grunndata.alternativprodukter.index

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.register.leaderelection.LeaderOnly
import org.slf4j.LoggerFactory

@Singleton
open class AlternativeProductScheduler(private val alternativeProductIndexer: AlternativeProductIndexer) {

    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductScheduler::class.java)
    }



    @LeaderOnly
    @Scheduled(cron = "0 0 1 * * *")
    open fun runReIndexAlternativeProducts() = runBlocking {
        LOG.info("Running re-index scheduler of alternative products")
        alternativeProductIndexer.reIndexAllDinstinctHmsNr()
    }
}