package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.TypeDef
import io.micronaut.data.model.DataType
import io.micronaut.serde.annotation.Serdeable
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

@Serdeable
data class ProductStockDTO(
    val id: UUID,
    val hmsArtNr: String,
    val status: ProductStockStatus,
    val warehouseStock: List<WarehouseStock>,
    val updated: LocalDateTime = LocalDateTime.now()
)

@Serdeable
data class WarehouseStock(
    val inStock: Boolean,
    val amountInStock: Int,
    val locationId: Int,
    val location: String,
    val available: Int,
    val reserved: Int,
    val needNotified: Int,
    val orders: Int,
    val request: Int,
    val minmax: Boolean,
    val updated: LocalDateTime,
    val backOrders: Int,
    val intRequest: Int,
    val physical: Int,
    )

fun ProductStock.toDTO(): ProductStockDTO {
    return ProductStockDTO(
        id = id,
        hmsArtNr = hmsArtnr,
        status = status,
        warehouseStock =  oebsStockResponse.map { it.toDTO(this.updated) },
        updated = updated
    )
}

fun OebsStockResponse.toDTO(updated: LocalDateTime): WarehouseStock {
    return WarehouseStock(
        inStock = erPåLager,
        amountInStock = antallPåLager,
        locationId = organisasjons_id,
        location = organisasjons_navn.substring(4),
        available = tilgjengelig,
        reserved = reservert,
        needNotified = behovsmeldt,
        orders = bestillinger,
        backOrders = restordre,
        request = anmodning,
        intRequest = intanmodning,
        minmax = minmax,
        physical = fysisk,
        updated = updated
    )
}


