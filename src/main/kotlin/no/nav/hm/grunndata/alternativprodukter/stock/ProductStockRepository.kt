package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import no.nav.hm.grunndata.alternativprodukter.alternative.ProductStockAlternatives
import java.util.UUID


@JdbcRepository(dialect = Dialect.POSTGRES)
interface ProductStockRepository : CoroutineCrudRepository<ProductStock, UUID> {
    suspend fun findByHmsArtnr(hmsArtnr: String): ProductStock?
}