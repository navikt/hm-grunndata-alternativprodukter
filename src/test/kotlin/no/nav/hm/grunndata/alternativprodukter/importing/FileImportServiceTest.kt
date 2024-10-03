package no.nav.hm.grunndata.alternativprodukter.importing

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@MicronautTest
class FileImportServiceTest(
    private val fileImportService: FileImportService,
    private val fileImportHistoryRepository: FileImportHistoryRepository,
) {

    @Test
    fun `importNewFiles should import new files and store handled filename`() {

        runBlocking {
            fileImportService.importNewFiles("src/test/resources/substituttlister/")
            fileImportHistoryRepository.findAll().toList().size shouldBe 3
        }


    }

}