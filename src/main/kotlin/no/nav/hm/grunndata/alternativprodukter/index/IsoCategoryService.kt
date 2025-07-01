package no.nav.hm.grunndata.alternativprodukter.index

import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO

@Singleton
class IsoCategoryService(private val gdbApiClient: GdbApiClient) {

    private val isoCategories: Map<String, IsoCategoryDTO> = runBlocking {
        gdbApiClient.retrieveIsoCategories().associateBy { it.isoCode }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(IsoCategoryService::class.java)
    }

    init {
        LOG.info("Got isoCategories: ${isoCategories.size}")
    }

    fun lookUpCode(isoCode: String): IsoCategoryDTO? = isoCategories[isoCode]


}


