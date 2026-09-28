package no.nav.hm.grunndata.alternativprodukter.index

import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import no.nav.hm.grunndata.rapid.dto.IsoCategory22DTO
import org.slf4j.LoggerFactory

@Singleton
class IsoCategory22Service(private val gdbApiClient: GdbApiClient) {

    private val isoCategories: Map<String, IsoCategory22DTO> = runBlocking {
        gdbApiClient.retrieveIsoCategories22().associateBy { it.isoCode }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategory22Service::class.java)
    }

    init {
        LOG.info("Got v2022 isoCategories: ${isoCategories.size}")
    }

    fun lookUpCode(isoCode: String): IsoCategory22DTO? = isoCategories[isoCode]


}


