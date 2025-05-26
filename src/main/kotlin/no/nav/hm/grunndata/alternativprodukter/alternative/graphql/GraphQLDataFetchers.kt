package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import jakarta.inject.Singleton
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductDoc

@Singleton
class GraphQLDataFetchers(private val alternativeQueryResolver: AlternativeQueryResolver) {

    fun getAlternativeProductsDataFetcher(): DataFetcher<List<AlternativeProductDoc>> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
                val hmsArtnr: List<String> =  dataFetchingEnvironment.getArgument("hmsnrs") ?: emptyList()
                alternativeQueryResolver.searchAlternativeProducts(hmsArtnr!!)
        }
    }

}