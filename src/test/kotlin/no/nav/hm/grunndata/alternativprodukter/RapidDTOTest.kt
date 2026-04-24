package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import org.junit.jupiter.api.Test

@MicronautTest
class RapidDTOTest(private val objectMapper: ObjectMapper) {

    @Test
    fun testRapidDTO() {
        val isoCategoryDTO = IsoCategoryDTO(
            isoCode = "123",
            isoTitle = "Test ISO Title",
            isoText = "Test ISO Text",
            isoLevel = 4
        )
        val asString = objectMapper.writeValueAsString(isoCategoryDTO)
        val asObject = objectMapper.readValue<IsoCategoryDTO>(asString, IsoCategoryDTO::class.java)
    }
}