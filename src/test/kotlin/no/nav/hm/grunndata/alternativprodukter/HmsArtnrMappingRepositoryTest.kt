package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.alternativprodukter.alternativ.HmsArtnrMappingRepository

@MicronautTest
class HmsArtnrMappingRepositoryTest(private val hmsArtnrMappingRepository: HmsArtnrMappingRepository) {
    @Test
    fun `insertMapping should add new mapping`() = runBlocking {
        hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), "123456", "654321")
        val read = hmsArtnrMappingRepository.findBySourceHmsArtnr("123456")
        assertEquals(1, read.size)
        assertEquals("654321", read[0].targetHmsArtnr)
    }

    @Test
    fun `insert and read mappings`() = runBlocking {
        hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), "123456", "654321")
        hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), "123456", "765432")
        val read = hmsArtnrMappingRepository.findBySourceHmsArtnr("123456")
        assertEquals(2, read.size)
    }

    @Test
    fun `findBySourceHmsArtnr should return empty list if no mapping found`() = runBlocking {
        val read = hmsArtnrMappingRepository.findBySourceHmsArtnr("999999")
        assertTrue(read.isEmpty())
    }
}
