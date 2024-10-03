package no.nav.hm.grunndata.alternativprodukter.importing

import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import kotlinx.coroutines.flow.toList
import no.nav.hm.grunndata.alternativprodukter.parser.ExcelParser
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path

@Singleton
open class FileImportService(
    private val fileImportHistoryRepository: FileImportHistoryRepository,
) {
    companion object {
        private val LOG = LoggerFactory.getLogger(FileImportService::class.java)
    }

    @Transactional
    open suspend fun importNewFiles(directoryPath: String) {
        val path = Paths.get(directoryPath)
        val allFilesInDirectory = Files.list(path).map { it.fileName.toString() }.toList()

        val importedFiles = fileImportHistoryRepository.findAll().toList().map { it.filename }

        val filesToImport = allFilesInDirectory.filter { it !in importedFiles }

        if (filesToImport.isEmpty()) {
            LOG.info("No new files to import")
            return
        } else {
            LOG.info("Found ${filesToImport.size} new files to import")
        }

        filesToImport.forEach { fileName ->
            LOG.info("Importing file $fileName")
            val filePath = Path(directoryPath, fileName)
            val parseResult = ExcelParser().readExcel(filePath.toString())

            val i = parseResult
            // Import the file
            // After successful import, add the file name to the fileImportHistoryRepository
        }
    }
}
