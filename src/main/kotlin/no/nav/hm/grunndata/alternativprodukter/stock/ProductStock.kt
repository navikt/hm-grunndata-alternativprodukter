package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.alternativprodukter.oebs.OebsStockResponse

@MappedEntity
data class ProductStock(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val hmsArtnr: String,
    val status: ProductStockStatus = ProductStockStatus.ACTIVE,
    @field:TypeDef(type = DataType.JSON)
    val oebsStockResponse: List<OebsStockResponse>,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
    val expired: LocalDateTime? = null,
)

enum class ProductStockStatus {
    ACTIVE, INACTIVE
}




