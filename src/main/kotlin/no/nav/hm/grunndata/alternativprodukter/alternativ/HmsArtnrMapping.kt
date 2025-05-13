package no.nav.hm.grunndata.alternativprodukter.alternativ

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDateTime
import java.util.UUID

@MappedEntity("hms_artnr_mapping")
data class HmsArtnrMapping(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val sourceHmsArtnr: String,
    val targetHmsArtnr: String,
    val created: LocalDateTime = LocalDateTime.now(),
)

@Serdeable
data class HmsArtnrMappingDto(
    val sourceHmsArtnr: String,
    val targetHmsArtnr: String,
)

fun HmsArtnrMapping.toDto() = HmsArtnrMappingDto(sourceHmsArtnr, targetHmsArtnr)
