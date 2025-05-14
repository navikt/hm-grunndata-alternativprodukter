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

data class ProductStockDTO(
    val id: UUID,
    val hmsArtnr: String,
    val status: ProductStockStatus,
    val stockQuantity: List<StockQuantity>,
    val updated: LocalDateTime = LocalDateTime.now()
)

data class StockQuantity(
    val inStock: Boolean,
    val amountInStock: Int,
    val location: String,
    val available: Int,
    val reserved: Int,
    val needNotified: Int,
    val orders: Int,
    val request: Int,
    val minmax: Boolean
)

fun ProductStock.toDTO(): ProductStockDTO {
    return ProductStockDTO(
        id = id,
        hmsArtnr = hmsArtnr,
        status = status,
        stockQuantity =  oebsStockResponse.map { it.toDTO() },
        updated = updated
    )
}

fun OebsStockResponse.toDTO(): StockQuantity {
    return StockQuantity(
        inStock = erPåLager,
        amountInStock = antallPåLager,
        location = organisasjons_navn.substring(4),
        available = tilgjengelig,
        reserved = reservert,
        needNotified = behovsmeldt,
        orders = bestillinger,
        request = anmodning,
        minmax = minmax
    )
}


