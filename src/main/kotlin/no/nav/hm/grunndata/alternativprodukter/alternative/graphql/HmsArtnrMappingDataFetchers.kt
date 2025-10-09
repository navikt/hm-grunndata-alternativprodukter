package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import jakarta.inject.Singleton
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMapping
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingInputDTO
import org.slf4j.LoggerFactory
import java.util.UUID

@Singleton
class HmsArtnrMappingDataFetchers(private val hmsArtnrMappingResolver: HmsArtnrMappingResolver,
                                  private val objectMapper: ObjectMapper
) {


    fun createHmsArtnrMappingDataFetcher(): DataFetcher<List<HmsArtnrMapping>> {
        return DataFetcher { env: DataFetchingEnvironment ->
            LOG.info("creating HmsArtnrMapping")
            val inputMap = env.getArgument<Map<String, Any>>("input")
            val input = objectMapper.convertValue(inputMap, HmsArtnrMappingInputDTO::class.java)
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
            val inputMap = env.getArgument<Map<String, Any>>("input")
            val input = objectMapper.convertValue(inputMap, HmsArtnrMappingInputDTO::class.java)
            LOG.info("deleting HmsArtnrMapping ${input.sourceHmsArtnr} ${input.targetHmsArtnr}")
            hmsArtnrMappingResolver.deleteHmsArtnrMapping(input)
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