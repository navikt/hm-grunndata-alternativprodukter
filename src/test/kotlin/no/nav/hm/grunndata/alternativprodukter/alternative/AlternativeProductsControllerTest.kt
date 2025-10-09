package no.nav.hm.grunndata.alternativprodukter.alternative

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

@MicronautTest
class AlternativeProductsControllerTest(private val alternativeProductsController: AlternativeProductsController) {

    val testHmsnr = "147286"

    @Test
    fun test() {
        runBlocking {
            val alts = alternativeProductsController.getAlternatives(testHmsnr)
            alts.shouldNotBeNull()
            alts.body().shouldNotBeNull()
            alts.body().original.hmsArtNr.shouldBe(testHmsnr)
        }
    }

    @Test
    fun `unknown hmsnr should return 404 not found`() {
        runBlocking {
            val response = alternativeProductsController.getAlternatives("0000")
            response.status.shouldBe(HttpStatus.NOT_FOUND)
        }
    }
}

@Language("JSON")
val jsontest =
    """
        {
  "seriesId": "59c3c17f-4580-4cb5-bccd-f79c286acb7b",
  "id": "6770583d-8281-4a9c-b5ef-c484406d92b3",
  "seriesTitle": "Sara Plus",
  "variantTitle": "Sara Plus",
  "status": "ACTIVE",
  "hmsArtNr": "147286",
  "imageUri": "orig/46234.jpg",
  "supplierName": "Arjo Norge AS",
  "highestRank": 99,
  "onAgreement": false,
  "warehouseStock": [
    {
      "location": "Østfold",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Oslo",
      "available": 2,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 2
    },
    {
      "location": "Hedmark",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Oppland",
      "available": 1,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 1
    },
    {
      "location": "Buskerud",
      "available": 2,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 2
    },
    {
      "location": "Vestfold",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Aust-Agder",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Vest-Agder",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Rogaland",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Hordaland",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Sogn og Fjordane",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Møre og Romsdal",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Sør-Trøndelag",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Nord-Trøndelag",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Nordland",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Troms",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    },
    {
      "location": "Finnmark",
      "available": 0,
      "reserved": 0,
      "needNotified": 0,
      "actualAvailable": 0
    }
  ],
  "inStockAnyWarehouse": true
}
"""