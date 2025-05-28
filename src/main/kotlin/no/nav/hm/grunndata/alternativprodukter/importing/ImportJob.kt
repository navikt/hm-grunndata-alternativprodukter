package no.nav.hm.grunndata.alternativprodukter.importing

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.micronaut.leaderelection.LeaderOnly
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
            try {
                LOG.info("Running alternative product mappings import job")
                fileImportService.importNewMappings()
                LOG.info("Alternative product mappings import job executed successfully")
            } catch (e: Exception) {
                LOG.error("Error while running alternative product mappings import job", e)
            }

        }

    }
}