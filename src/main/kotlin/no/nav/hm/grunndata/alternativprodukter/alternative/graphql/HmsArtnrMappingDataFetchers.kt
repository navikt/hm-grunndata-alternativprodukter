package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import io.micronaut.data.model.query.factory.Projections.id
import jakarta.inject.Singleton
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMapping
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingInputDTO
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class HmsArtnrMappingDataFetchers(private val hmsArtnrMappingResolver: HmsArtnrMappingResolver) {


    fun createHmsArtnrMappingDataFetcher(): DataFetcher<HmsArtnrMapping> {
        return DataFetcher { env: DataFetchingEnvironment ->
            LOG.info("creating HmsArtnrMapping")
            val input = env.getArgument<HmsArtnrMappingInputDTO>("input")
            hmsArtnrMappingResolver.createHmsArtnrMapping(input!!)
        }
    }

    fun updateHmsArtnrMappingDataFetcher() : DataFetcher<HmsArtnrMapping> {
        return DataFetcher { env: DataFetchingEnvironment ->
            val id = env.getArgument<UUID>("id")
            LOG.info("updating HmsArtnrMapping for $id")
            val input = env.getArgument<HmsArtnrMappingInputDTO>("input")
            hmsArtnrMappingResolver.updateHmsArtnrMapping(id!!, input!!)
        }
    }

    fun deleteHmsArtnrMappingDataFetcher() : DataFetcher<Boolean> {
        return DataFetcher { env: DataFetchingEnvironment ->
            val id = env.getArgument<UUID>("id")
            LOG.info("deleting HmsArtnrMapping for $id")
            ( hmsArtnrMappingResolver.deleteHmsArtnrMapping(id!!)  == 1 )
        }
    }

    fun getHmsArtnrMappingById() : DataFetcher<HmsArtnrMapping> {
        return DataFetcher { env ->
            val id = env.getArgument<UUID>("id")
            LOG.info("getting HmsArtnrMapping for $id")
            hmsArtnrMappingResolver.getHmsArtnrMappingById(id!!)
        }
    }

    fun getHmsArtnrMappingBySourceHmsArtnr(): DataFetcher<List<HmsArtnrMapping>> {
        return DataFetcher { env ->
            val sourceHmsArtnr = env.getArgument<String>("sourceHmsArtnr")
            LOG.info("getting HmsArtnrMapping for $sourceHmsArtnr")
            hmsArtnrMappingResolver.getHmsArtnrMappingBySourceHmsArtnr(sourceHmsArtnr!!)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(HmsArtnrMappingDataFetchers::class.java)
    }
}