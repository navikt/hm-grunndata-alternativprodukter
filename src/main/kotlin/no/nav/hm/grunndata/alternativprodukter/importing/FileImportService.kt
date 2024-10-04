package no.nav.hm.grunndata.alternativprodukter.importing

import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import no.nav.hm.grunndata.alternativprodukter.AlternativeProductsService
import no.nav.hm.grunndata.alternativprodukter.parser.ExcelParser
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path

@Singleton
open class FileImportService(
    private val fileImportHistoryRepository: FileImportHistoryRepository,
    private val alternativeProductsService: AlternativeProductsService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(FileImportService::class.java)
    }

    @Transactional
    open suspend fun importNewMappings(directoryPath: String) = withContext(Dispatchers.IO) {
        val url = this::class.java.classLoader.getResource(directoryPath)
        if (url == null) {
            LOG.error("Directory $directoryPath not found in classpath")
            return@withContext
        }

        val uri = url.toURI()
        val path = Paths.get(uri)
        val allFilesInDirectory = Files.list(path).map { it.fileName.toString() }.toList().filter { !it.startsWith("~") }.sorted()

        val allFilesStartWithVAndNumber = allFilesInDirectory.all { it.matches(Regex("^V\\d+.*")) }

        if (!allFilesStartWithVAndNumber) {
            LOG.error("Not all files start with an uppercase V followed by a number (version number)")
        }

        val importedFiles = fileImportHistoryRepository.findAll().toList().map { it.filename }

        val filesToImport = allFilesInDirectory.filter { it !in importedFiles }

        if (filesToImport.isEmpty()) {
            LOG.info("No new files to import")
            return@withContext
        } else {
            LOG.info("Found ${filesToImport.size} new files to import")
        }


        filesToImport.forEach { fileName ->
            LOG.info("Importing file $fileName")

            // Get the InputStream for the file from the classpath
            val inputStream = this::class.java.classLoader.getResourceAsStream("$directoryPath/$fileName")
            if (inputStream == null) {
                LOG.error("File $fileName not found in classpath")
                return@forEach
            }

            // Use the InputStream to read the file
            val parseResult = ExcelParser().readExcel(inputStream)

            parseResult.addGroups.map { addGroup ->
                alternativeProductsService.saveAlternativeProducts(addGroup)
            }

            parseResult.removeGroups.map { removeGroup ->
                alternativeProductsService.deleteAlternativeProducts(removeGroup)
            }

            fileImportHistoryRepository.save(FileImportHistory(filename = fileName))
        }
    }

}
