package no.nav.hm.grunndata.alternativprodukter.importing

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDateTime
import java.util.UUID

@JdbcRepository(dialect = Dialect.POSTGRES)
interface FileImportHistoryRepository :
    CoroutineCrudRepository<FileImportHistory, UUID>,
    CoroutineJpaSpecificationExecutor<FileImportHistory>

@MappedEntity("file_import_history")
data class FileImportHistory(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val filename: String,
    val created: LocalDateTime = LocalDateTime.now(),
)
