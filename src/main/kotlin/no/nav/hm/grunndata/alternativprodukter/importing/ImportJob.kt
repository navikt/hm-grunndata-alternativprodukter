package no.nav.hm.grunndata.alternativprodukter.importing

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.register.leaderelection.LeaderOnly
import org.slf4j.LoggerFactory

@Singleton
@Requires(property = "micronaut.task.scheduling.enabled", value = "true")
open class ImportJob(
    private val fileImportService: FileImportService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ImportJob::class.java)
    }

    @LeaderOnly
    @Scheduled(initialDelay = "10s")
    open fun importNewAlternativeProductMappings() {
        runBlocking {
            LOG.info("Running alternative product mappings import job")
            val directoryPath = "src/main/resources/substituttlister/"
            fileImportService.importNewMappings(directoryPath)
            LOG.info("Alternative product mappings import job executed successfully")
        }

    }
}