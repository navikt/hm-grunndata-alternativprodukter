package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.runtime.Micronaut
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "hm-grunndata-alternativprodukter",
        version = "1.0",
        description = "API for Alternativprodukter"
    )
)
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
            .packages("no.nav.hm.grunndata.alternativprodukter")
            .mainClass(Application.javaClass)
            .start()
    }
}

