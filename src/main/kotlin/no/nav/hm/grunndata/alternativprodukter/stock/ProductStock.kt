package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.alternativprodukter.oebs.WarehouseStockResponse

@MappedEntity
data class ProductStock(
    @field:Id
    val id: UUID = UUID.randomUUID(),
    val hmsArtnr: String,
    @field:TypeDef(type = DataType.JSON)
    val warehouseStockResponse: List<WarehouseStockResponse>,
    val created: LocalDateTime = LocalDateTime.now(),
    val updated: LocalDateTime = LocalDateTime.now(),
)




