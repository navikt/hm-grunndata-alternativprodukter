package no.nav.hm.grunndata.alternativprodukter.alternative

import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface HmsArtnrMappingRepository : CoroutineCrudRepository<HmsArtnrMapping, UUID> {

    @Query(
        """INSERT INTO hms_artnr_mapping (id, source_hms_artnr, target_hms_artnr) VALUES (:id, :sourceHmsArtnr, :targetHmsArtnr)""",
    )
    suspend fun insertMapping(
        id: UUID,
        sourceHmsArtnr: String,
        targetHmsArtnr: String,
    )

    @Query("""DELETE FROM hms_artnr_mapping WHERE source_hms_artnr = :sourceHmsArtnr AND target_hms_artnr = :targetHmsArtnr""")
    suspend fun deleteMapping(sourceHmsArtnr: String, targetHmsArtnr: String)

    @Query("""SELECT * FROM hms_artnr_mapping WHERE source_hms_artnr = :sourceHmsArtnr""")
    suspend fun findBySourceHmsArtnr(sourceHmsArtnr: String): List<HmsArtnrMapping>

    @Query("""SELECT * FROM hms_artnr_mapping WHERE source_hms_artnr = :sourceHmsArtnr AND target_hms_artnr = :targetHmsArtnr""")
    suspend fun findBySourceHmsArtnrAndTargetHmsArtnr(sourceHmsArtnr: String, targetHmsArtnr: String): HmsArtnrMapping?

    suspend fun findDistinctSourceHmsArtnr(): Set<String>

    @Query(
        """
        SELECT DISTINCT
            LEAST(m1.source_hms_artnr, m1.target_hms_artnr) AS a,
            GREATEST(m1.source_hms_artnr, m1.target_hms_artnr) AS b
        FROM hms_artnr_mapping m1
        JOIN hms_artnr_mapping m2
          ON m1.source_hms_artnr = m2.target_hms_artnr
         AND m1.target_hms_artnr = m2.source_hms_artnr
        WHERE :hmsArtNr IN (m1.source_hms_artnr, m1.target_hms_artnr,
                            m2.source_hms_artnr, m2.target_hms_artnr)
        """
    )
    suspend fun findSymmetricPairsForHmsArtNr(hmsArtNr: String): List<SymmetricPair>

    @Query(
        """SELECT DISTINCT
               LEAST(m1.source_hms_artnr, m1.target_hms_artnr) AS a,
               GREATEST(m1.source_hms_artnr, m1.target_hms_artnr) AS b
           FROM hms_artnr_mapping m1
           JOIN hms_artnr_mapping m2
             ON m1.source_hms_artnr = m2.target_hms_artnr
            AND m1.target_hms_artnr = m2.source_hms_artnr"""
    )
    suspend fun findAllSymmetricPairs(): List<SymmetricPair>
}

@Serdeable
data class SymmetricPair(
    val a: String,
    val b: String,
)
