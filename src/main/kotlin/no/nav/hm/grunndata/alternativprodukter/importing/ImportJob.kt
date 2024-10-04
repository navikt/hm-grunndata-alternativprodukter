package no.nav.hm.grunndata.alternativprodukter.importing

import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Singleton
@Requires(property = "micronaut.task.scheduling.enabled", value = "true")
class ImportJob(
    private val fileImportService: FileImportService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ImportJob::class.java)
    }

    @Scheduled(initialDelay = "10s")
    fun execute() {
        runBlocking {
            LOG.info("Executing job")
            val directoryPath = "src/main/resources/substituttlister/"
            fileImportService.importNewMappings(directoryPath)
            LOG.info("Job executed successfully")
        }

    }
}