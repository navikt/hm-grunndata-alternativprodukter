package no.nav.hm.grunndata.alternativprodukter.importing

import io.kotest.matchers.shouldBe
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingRepository
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
            fileImportService.importNewMappings(SubstituteFiles.entries.map { it.fileName })
            fileImportHistoryRepository.findAll().toList().size shouldBe 24

            // Personløftere
            hmsArtnrMappingRepository.findBySourceHmsArtnr("232472").size shouldBe 5
            hmsArtnrMappingRepository.findBySourceHmsArtnr("211366").size shouldBe 5
            hmsArtnrMappingRepository.findBySourceHmsArtnr("148028").size shouldBe 11

            // Stoler med oppreisingsfunksjon
            hmsArtnrMappingRepository.findBySourceHmsArtnr("296150").size shouldBe 3
            hmsArtnrMappingRepository.findBySourceHmsArtnr("240534").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("296285").size shouldBe 5

            // Kalendere
            hmsArtnrMappingRepository.findBySourceHmsArtnr("267614").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("286501").size shouldBe 4
            hmsArtnrMappingRepository.findBySourceHmsArtnr("292363").size shouldBe 1

            // Varmehjelpemidler
            hmsArtnrMappingRepository.findBySourceHmsArtnr("252780").size shouldBe 3
            hmsArtnrMappingRepository.findBySourceHmsArtnr("252891").size shouldBe 1
            hmsArtnrMappingRepository.findBySourceHmsArtnr("252930").size shouldBe 1

            // Arbeidsstoler
            hmsArtnrMappingRepository.findBySourceHmsArtnr("323899").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("268728").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("324130").size shouldBe 2

            // Syn
            hmsArtnrMappingRepository.findBySourceHmsArtnr("291885").size shouldBe 1
            hmsArtnrMappingRepository.findBySourceHmsArtnr("194564").size shouldBe 9
            hmsArtnrMappingRepository.findBySourceHmsArtnr("233032").size shouldBe 2

            // Ganghjelpemidler
            hmsArtnrMappingRepository.findBySourceHmsArtnr("147600").size shouldBe 1
            hmsArtnrMappingRepository.findBySourceHmsArtnr("177946").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("214762").size shouldBe 6
            hmsArtnrMappingRepository.findBySourceHmsArtnr("313241").size shouldBe 1

            // Manuelle rullestoler DK6
            hmsArtnrMappingRepository.findBySourceHmsArtnr("326559").size shouldBe 1
            hmsArtnrMappingRepository.findBySourceHmsArtnr("326545").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("326554").size shouldBe 2

            // ERS
            hmsArtnrMappingRepository.findBySourceHmsArtnr("301993").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("301573").size shouldBe 9
            hmsArtnrMappingRepository.findBySourceHmsArtnr("304603").size shouldBe 7

            // Vogner
            hmsArtnrMappingRepository.findBySourceHmsArtnr("325239").size shouldBe 9
            hmsArtnrMappingRepository.findBySourceHmsArtnr("324995").size shouldBe 17
            hmsArtnrMappingRepository.findBySourceHmsArtnr("286056").size shouldBe 7

            // Kjøreramper
            hmsArtnrMappingRepository.findBySourceHmsArtnr("014112").size shouldBe 1
            hmsArtnrMappingRepository.findBySourceHmsArtnr("297070").size shouldBe 14
            hmsArtnrMappingRepository.findBySourceHmsArtnr("297095").size shouldBe 3
            hmsArtnrMappingRepository.findBySourceHmsArtnr("297075").size shouldBe 3

            // Ståstativ, trenings og aktiviseringshjelpemidler
            hmsArtnrMappingRepository.findBySourceHmsArtnr("327685").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("292303").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("327660").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("328007").size shouldBe 1

            // Senger
            hmsArtnrMappingRepository.findBySourceHmsArtnr("311643").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("311846").size shouldBe 4
            hmsArtnrMappingRepository.findBySourceHmsArtnr("311636").size shouldBe 8

            //Kjøreposer
            hmsArtnrMappingRepository.findBySourceHmsArtnr("322327").size shouldBe 6
            hmsArtnrMappingRepository.findBySourceHmsArtnr("100812").size shouldBe 4

            //Madrasser
            hmsArtnrMappingRepository.findBySourceHmsArtnr("313971").size shouldBe 4
            hmsArtnrMappingRepository.findBySourceHmsArtnr("314075").size shouldBe 8

            //Manuelle rullestoler DK9
            hmsArtnrMappingRepository.findBySourceHmsArtnr("316159").size shouldBe 11
            hmsArtnrMappingRepository.findBySourceHmsArtnr("316160").size shouldBe 9

            // Løfteplattformer oh hjepemidler i trapp
            hmsArtnrMappingRepository.findBySourceHmsArtnr("230747").size shouldBe 3
            hmsArtnrMappingRepository.findBySourceHmsArtnr("326350").size shouldBe 7
            hmsArtnrMappingRepository.findBySourceHmsArtnr("324159").size shouldBe 6
            hmsArtnrMappingRepository.findBySourceHmsArtnr("325317").size shouldBe 3

            // Sykler
            hmsArtnrMappingRepository.findBySourceHmsArtnr("315323").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("315797").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("315024").size shouldBe 7

            // Hørsel
            hmsArtnrMappingRepository.findBySourceHmsArtnr("233310").size shouldBe 1
            hmsArtnrMappingRepository.findBySourceHmsArtnr("291302").size shouldBe 12
            hmsArtnrMappingRepository.findBySourceHmsArtnr("328207").size shouldBe 3

            // Kommunikasjon
            hmsArtnrMappingRepository.findBySourceHmsArtnr("290584").size shouldBe 1
            hmsArtnrMappingRepository.findBySourceHmsArtnr("291459").size shouldBe 8
            hmsArtnrMappingRepository.findBySourceHmsArtnr("327039").size shouldBe 6

            // Sittemoduler
            hmsArtnrMappingRepository.findBySourceHmsArtnr("294635").size shouldBe 1
            hmsArtnrMappingRepository.findBySourceHmsArtnr("295083").size shouldBe 3
            hmsArtnrMappingRepository.findBySourceHmsArtnr("294670").size shouldBe 2

            // Hjelpemidler for overflytting, vending og posisjonering
            hmsArtnrMappingRepository.findBySourceHmsArtnr("311964").size shouldBe 1
            hmsArtnrMappingRepository.findBySourceHmsArtnr("164188").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("311970").size shouldBe 3

            // Arbeidsstoler oppdatert
            hmsArtnrMappingRepository.findBySourceHmsArtnr("324092").size shouldBe 2

            // Hygiene
            hmsArtnrMappingRepository.findBySourceHmsArtnr("269836").size shouldBe 2
            hmsArtnrMappingRepository.findBySourceHmsArtnr("297571").size shouldBe 2

        }


    }

}