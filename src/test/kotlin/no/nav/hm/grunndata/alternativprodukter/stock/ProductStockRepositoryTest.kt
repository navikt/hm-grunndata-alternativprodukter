package no.nav.hm.grunndata.alternativprodukter.stock

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@MicronautTest
class ProductStockRepositoryTest(private val productStockRepository: ProductStockRepository) {

    @Test
    fun testProductStockRepository() {
        ProductStock(
            hmsArtnr = "123456",
            warehouseStockResponse = emptyList(),
        ).let { productStock ->
            runBlocking {
                productStockRepository.save(productStock)
                val foundProductStock = productStockRepository.findByHmsArtnr("123456")
                assertNotNull(foundProductStock)
                assert(foundProductStock!!.hmsArtnr == "123456")
                val updated = productStockRepository.update(foundProductStock.copy(updated = LocalDateTime.now()))
                assertNotNull(updated)
            }
        }
    }

}