package no.nav.hm.grunndata.alternativprodukter.alternative.graphql

import graphql.GraphQL
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import graphql.schema.idl.TypeRuntimeWiring
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.core.io.ResourceResolver
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import jakarta.inject.Singleton

@Factory
class GraphQLFactory {

    @Bean
    @Singleton
    fun graphQL(resourceResolver: ResourceResolver,
                graphQLDataFetchers: GraphQLDataFetchers,
                hmsArtnrMappingDataFetchers: HmsArtnrMappingDataFetchers
                ): GraphQL {
        val schemaParser = SchemaParser()

        val typeRegistry = TypeDefinitionRegistry()
        val graphqlSchema = resourceResolver.getResourceAsStream("classpath:graphql/schema.graphqls")

        return if (graphqlSchema.isPresent) {
            typeRegistry.merge(schemaParser.parse(BufferedReader(InputStreamReader(graphqlSchema.get()))))
            val runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                    .dataFetcher("alternativeProducts", graphQLDataFetchers.getAlternativeProductsDataFetcher())
                    .dataFetcher("productStock", graphQLDataFetchers.getProductStockDataFetcher())
                    .dataFetcher("getHmsArtnrMappingsById", hmsArtnrMappingDataFetchers.getHmsArtnrMappingById())
                    .dataFetcher("getHmsArtnrMappingBySourceHmsArtnr", hmsArtnrMappingDataFetchers.getHmsArtnrMappingBySourceHmsArtnr())
                )
                .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                    .dataFetcher("createHmsArtnrMapping", hmsArtnrMappingDataFetchers.createHmsArtnrMappingDataFetcher())
                    .dataFetcher("updateHmsArtnrMapping", hmsArtnrMappingDataFetchers.updateHmsArtnrMappingDataFetcher())
                    .dataFetcher("deleteHmsArtnrMapping", hmsArtnrMappingDataFetchers.deleteHmsArtnrMappingDataFetcher())
                )
                .build()
            val schemaGenerator = SchemaGenerator()
            val graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring)
            GraphQL.newGraphQL(graphQLSchema).build()
        } else {
            LOG.debug("No GraphQL services found, returning empty schema")
            GraphQL.Builder(GraphQLSchema.newSchema().build()).build()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(GraphQLFactory::class.java)
    }
}