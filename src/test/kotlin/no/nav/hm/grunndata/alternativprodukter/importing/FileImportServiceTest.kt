package no.nav.hm.grunndata.alternativprodukter.importing

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.HmsArtnrMappingRepository
import org.junit.jupiter.api.Test

@MicronautTest
class FileImportServiceTest(
    private val fileImportService: FileImportService,
    private val fileImportHistoryRepository: FileImportHistoryRepository,
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository
) {

    @Test
    fun `importNewFiles should import new files and store handled filename`() {

        runBlocking {
            fileImportService.importNewMappings(SubstituteFilesTest.values().map { it.fileName })
            fileImportHistoryRepository.findAll().toList().size shouldBe 3

            hmsArtnrMappingRepository.findBySourceHmsArtnr("232472").size shouldBe 2

            hmsArtnrMappingRepository.findBySourceHmsArtnr("014760").size shouldBe 6
            hmsArtnrMappingRepository.findBySourceHmsArtnr("196087").size shouldBe 6



        }


    }

}