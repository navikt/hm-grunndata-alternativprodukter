package no.nav.hm.grunndata.alternativprodukter.alternative

import jakarta.inject.Singleton

@Singleton
class CliqueService(
    private val hmsArtnrMappingRepository: HmsArtnrMappingRepository,
) {

    /**
     * Find all maximal cliques (size >= 2) in the symmetric ego-network of [hmsArtNr]
     * that contain [hmsArtNr]. https://en.wikipedia.org/wiki/Bron%E2%80%93Kerbosch_algorithm
     */
    suspend fun findCliquesContaining(hmsArtNr: String): Set<Set<String>> {
        // Load all symmetric pairs in the table
        val allPairs = hmsArtnrMappingRepository.findAllSymmetricPairs()
            .map { pair ->
                val a = pair.a
                val b = pair.b
                if (a <= b) a to b else b to a
            }
            .toSet()

        if (allPairs.isEmpty()) return emptySet()

        // First, find the ego nodes: hmsArtNr + its direct symmetric neighbors
        val directNeighbors = allPairs.flatMap { (a, b) ->
            when (hmsArtNr) {
                a -> listOf(b)
                b -> listOf(a)
                else -> emptyList()
            }
        }.toSet()

        if (directNeighbors.isEmpty()) return emptySet()

        val egoNodes = (directNeighbors + hmsArtNr).sorted().toSet()

        // Restrict pairs to those fully inside the ego-node set (induced subgraph)
        val symmetricPairs = allPairs.filter { (a, b) ->
            a in egoNodes && b in egoNodes
        }.toSet()

        // Build undirected adjacency from symmetric pairs
        val neighbors = mutableMapOf<String, MutableSet<String>>()
        for ((a, b) in symmetricPairs) {
            neighbors.computeIfAbsent(a) { mutableSetOf() }.add(b)
            neighbors.computeIfAbsent(b) { mutableSetOf() }.add(a)
        }
        // Ensure the requested node is present even if it has no neighbors inside the induced graph
        neighbors.putIfAbsent(hmsArtNr, mutableSetOf())

        val graph = neighbors.mapValues { it.value.toSet() }
        val allCliques = mutableSetOf<Set<String>>()

        // Guard against pathological recursion depth: ego graphs are normally tiny,
        // but if something goes wrong we avoid blowing the stack.
        val maxDepth = egoNodes.size + 2

        fun bronKerbosch(r: Set<String>, p: Set<String>, x: Set<String>, depth: Int) {
            if (depth > maxDepth) return

            if (p.isEmpty() && x.isEmpty()) {
                if (r.contains(hmsArtNr) && r.size >= 2) {
                    allCliques.add(r)
                }
                return
            }

            var pVar = p
            for (v in p.toList()) {
                val vNeighbors = graph[v] ?: emptySet()
                if (vNeighbors.isEmpty()) {
                    // If v has no neighbors in the induced graph, it cannot grow a clique beyond itself
                    pVar = pVar - v
                    continue
                }
                bronKerbosch(
                    r + v,
                    pVar intersect vNeighbors,
                    x intersect vNeighbors,
                    depth + 1,
                )
                pVar = pVar - v
            }
        }

        bronKerbosch(emptySet(), egoNodes, emptySet(), depth = 0)

        // Keep only maximal cliques (no clique that is a strict subset of another)
        val maximalCliques = allCliques.filter { candidate ->
            allCliques.none { other ->
                other !== candidate && other.containsAll(candidate)
            }
        }.toSet()

        return maximalCliques
    }
}
