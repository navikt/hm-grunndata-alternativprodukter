package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import jakarta.inject.Singleton
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMapping
import no.nav.hm.grunndata.alternativprodukter.alternative.HmsArtnrMappingInputDTO
import java.util.UUID

@Singleton
class HmsArtnrMappingDataFetchers(private val hmsArtnrMappingResolver: HmsArtnrMappingResolver) {

    fun createHmsArtnrMappingDataFetcher(): DataFetcher<HmsArtnrMapping> {
        return DataFetcher { env: DataFetchingEnvironment ->
            val input = env.getArgument<HmsArtnrMappingInputDTO>("input")
            hmsArtnrMappingResolver.createHmsArtnrMapping(input!!)
        }
    }

    fun updateHmsArtnrMappingDataFetcher() : DataFetcher<HmsArtnrMapping> {
        return DataFetcher { env: DataFetchingEnvironment ->
            val id = env.getArgument<UUID>("id")
            val input = env.getArgument<HmsArtnrMappingInputDTO>("input")
            hmsArtnrMappingResolver.updateHmsArtnrMapping(id!!, input!!)
        }
    }

    fun deleteHmsArtnrMappingDataFetcher() : DataFetcher<Boolean> {
        return DataFetcher { env: DataFetchingEnvironment ->
            val id = env.getArgument<UUID>("id")
            ( hmsArtnrMappingResolver.deleteHmsArtnrMapping(id!!)  == 1 )
        }
    }

    fun getHmsArtnrMappingById() : DataFetcher<HmsArtnrMapping> {
        return DataFetcher { env ->
            val id = env.getArgument<UUID>("id")
            hmsArtnrMappingResolver.getHmsArtnrMappingById(id!!)
        }
    }

    fun getHmsArtnrMappingBySourceHmsArtnr(): DataFetcher<List<HmsArtnrMapping>> {
        return DataFetcher { env ->
            val sourceHmsArtnr = env.getArgument<String>("sourceHmsArtnr")
            hmsArtnrMappingResolver.getHmsArtnrMappingBySourceHmsArtnr(sourceHmsArtnr!!)
        }
    }

}