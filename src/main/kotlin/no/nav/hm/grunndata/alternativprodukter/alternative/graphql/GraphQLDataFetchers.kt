package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import jakarta.inject.Singleton
import no.nav.hm.grunndata.alternativprodukter.index.AlternativeProductDoc
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockDTO


@Singleton
class GraphQLDataFetchers(private val alternativeQueryResolver: AlternativeQueryResolver) {

    fun getAlternativeProductsDataFetcher(): DataFetcher<List<AlternativeProductDoc>> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
                val hmsArtnr: List<String> =  dataFetchingEnvironment.getArgument("hmsnrs") ?: emptyList()
                alternativeQueryResolver.searchAlternativeProducts(hmsArtnr!!)
        }
    }

    fun getProductStockDataFetcher(): DataFetcher<ProductStockDTO> {
        return DataFetcher {
            dataFetchingEnvironment: DataFetchingEnvironment ->
            val hmsNr: String =  dataFetchingEnvironment.getArgument("hmsnr") ?: ""
            alternativeQueryResolver.getProductStock(hmsNr)
        }
    }

    fun getProductStocksByEnhetNrDataFetcher(): DataFetcher<List<ProductStockDTO>> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            val hmsNrs: List<String> =  dataFetchingEnvironment.getArgument("hmsnrs") ?: emptyList()
            val enhetnr: String = dataFetchingEnvironment.getArgument("enhetnr") ?: ""
            alternativeQueryResolver.getProductStocksByEnhetNr(hmsNrs.toSet(), enhetnr)
        }
    }

    fun getProductStocksDataFetcher(): DataFetcher<List<ProductStockDTO>> {
        return DataFetcher { dataFetchingEnvironment: DataFetchingEnvironment ->
            val hmsNrs: List<String> =  dataFetchingEnvironment.getArgument("hmsnrs") ?: emptyList()
            alternativeQueryResolver.getProductStocks(hmsNrs.toSet())
        }
    }

}