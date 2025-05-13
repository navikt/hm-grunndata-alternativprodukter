package no.nav.hm.grunndata.alternativprodukter.importing

import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import no.nav.hm.grunndata.alternativprodukter.alternative.AlternativeProductsService
import no.nav.hm.grunndata.alternativprodukter.parser.ExcelParser
import org.slf4j.LoggerFactory

@Singleton
open class FileImportService(
    private val fileImportHistoryRepository: FileImportHistoryRepository,
    private val alternativeProductsService: AlternativeProductsService
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(FileImportService::class.java)
    }

    @Transactional
    open suspend fun importNewMappings(importFiles: List<String> = SubstituteFiles.values().map { it.fileName }) =
        withContext(Dispatchers.IO) {


            val allFilesStartWithVAndNumber = importFiles.all { it.matches(Regex("^V\\d+.*")) }

            if (!allFilesStartWithVAndNumber) {
                LOG.error("Not all files start with an uppercase V followed by a number (version number)")
                throw IllegalArgumentException("Not all files start with an uppercase V followed by a number (version number)")
            }

            val importedFiles = fileImportHistoryRepository.findAll().toList().map { it.filename }
            val filesToImport = importFiles.filter { it !in importedFiles }

            if (filesToImport.isEmpty()) {
                LOG.info("No new files to import")
                return@withContext
            } else {
                LOG.info("Found ${filesToImport.size} new files to import")
            }


            filesToImport.forEach { fileName ->
                LOG.info("Importing file $fileName")

                // Get the InputStream for the file from the classpath
                val inputStream = this::class.java.classLoader.getResourceAsStream("substituttlister/$fileName")
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
