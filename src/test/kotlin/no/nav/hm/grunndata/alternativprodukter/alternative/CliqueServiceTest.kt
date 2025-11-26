package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

@MicronautTest
class CliqueServiceTest {

    @Inject
    lateinit var hmsArtnrMappingRepository: HmsArtnrMappingRepository

    @Inject
    lateinit var cliqueService: CliqueService

    @Test
    fun `find maximal cliques for 999991`() = runBlocking {
        // Arrange: set up the example mappings for 999991
        val a = "999991"
        val b = "999992"
        val c = "999993"
        val d = "999994"

        suspend fun insert(s: String, t: String) {
            hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), s, t)
        }

        insert(a, b)
        insert(b, a)
        insert(a, c)
        insert(c, a)
        insert(d, a)
        insert(a, d)
        insert(d, c)
        insert(c, d)

        // Act
        val cliques = cliqueService.findCliquesContaining(a)

        // Expect two maximal cliques: {999991, 999992} and {999991, 999993, 999994}
        val expected = setOf(
            setOf(a, b),
            setOf(a, c, d),
        )

        assertEquals(expected, cliques)
    }

    @Test
    fun `no mappings yields no cliques`() = runBlocking {
        val x = "X1"
        val cliques = cliqueService.findCliquesContaining(x)
        assertEquals(emptySet<Set<String>>(), cliques)
    }

    @Test
    fun `single symmetric pair yields one clique`() = runBlocking {
        val a = "A1"
        val b = "B1"

        suspend fun insert(s: String, t: String) {
            hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), s, t)
        }

        insert(a, b)
        insert(b, a)

        val cliques = cliqueService.findCliquesContaining(a)
        assertEquals(setOf(setOf(a, b)), cliques)
    }

    @Test
    fun `neighbors without neighbor link yield only pair cliques`() = runBlocking {
        val a = "C1"
        val b = "C2"
        val c = "C3"

        suspend fun insert(s: String, t: String) {
            hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), s, t)
        }

        // a<->b and a<->c, but no b<->c
        insert(a, b)
        insert(b, a)
        insert(a, c)
        insert(c, a)

        val cliques = cliqueService.findCliquesContaining(a)
        val expected = setOf(setOf(a, b), setOf(a, c))
        assertEquals(expected, cliques)
    }

    @Test
    fun `overlapping cliques around center`() = runBlocking {
        val a = "D1"
        val b = "D2"
        val c = "D3"
        val d = "D4"

        suspend fun insert(s: String, t: String) {
            hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), s, t)
        }

        // Clique {a,b,c}
        insert(a, b); insert(b, a)
        insert(a, c); insert(c, a)
        insert(b, c); insert(c, b)

        // Clique {a,c,d}
        insert(a, d); insert(d, a)
        insert(c, d); insert(d, c)

        val cliques = cliqueService.findCliquesContaining(a)

        val expected = setOf(
            setOf(a, b, c),
            setOf(a, c, d),
        )

        assertEquals(expected, cliques)
    }

    @Test
    fun `two cliques`() = runBlocking {
        val a = "D1"
        val b = "D2"
        val c = "D3"

        suspend fun insert(s: String, t: String) {
            hmsArtnrMappingRepository.insertMapping(UUID.randomUUID(), s, t)
        }

        // Clique {a,b}
        insert(a, b); insert(b, a)

        // Clique {c,b}
        insert(c, b); insert(b, c)

        val cliques = cliqueService.findCliquesContaining(b)

        val expected = setOf(
            setOf(a, b),
            setOf(c, b),
        )

        assertEquals(expected, cliques)

        //adding one more product to clique {a,b}
        val d = "D4"
        insert(a, d); insert(d, a)
        insert(b, d); insert(d, b)

        val expectedUpdated = setOf(
            setOf(a, b, d),
            setOf(c, b),
        )

        val cliquesUpdated = cliqueService.findCliquesContaining(b)

        assertEquals(expectedUpdated, cliquesUpdated)
    }
}
