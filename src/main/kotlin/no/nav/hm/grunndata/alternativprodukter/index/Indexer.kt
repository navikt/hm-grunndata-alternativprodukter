package no.nav.hm.grunndata.alternativprodukter.index


import org.opensearch.client.opensearch.OpenSearchClient
import org.opensearch.client.opensearch._types.Refresh
import org.opensearch.client.opensearch._types.mapping.TypeMapping
import org.opensearch.client.opensearch.core.*
import org.opensearch.client.opensearch.core.bulk.BulkOperation
import org.opensearch.client.opensearch.core.bulk.IndexOperation
import org.opensearch.client.opensearch.indices.*
import org.opensearch.client.opensearch.indices.ExistsRequest
import org.opensearch.client.opensearch.indices.update_aliases.ActionBuilders
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.time.LocalDate
import java.util.*

abstract class Indexer(private val client: OpenSearchClient,
                       private val settings: String,
                       private val mapping: String,
                       private val aliasName: String) {

    companion object {
        private val LOG = LoggerFactory.getLogger(Indexer::class.java)
    }


    init {
        try {
            initAlias()
        } catch (e: Exception) {
            LOG.error("Trying to init alias ${this@Indexer.aliasName}, failed! OpenSearch might not be ready ${e.message}, will wait 10s and retry")
            Thread.sleep(10000)
            initAlias()
        }
    }


    fun updateAlias(indexName: String): Boolean {
        val updateAliasesRequestBuilder = UpdateAliasesRequest.Builder()
        if (existsAlias()) {
            val aliasResponse = getAlias()
            val indices = aliasResponse.result().keys
            indices.forEach { index ->
                val removeAction = ActionBuilders.remove().index(index).alias(this@Indexer.aliasName).build()
                updateAliasesRequestBuilder.actions { it.remove(removeAction) }
            }
        }
        val addAction = ActionBuilders.add().index(indexName).alias(this@Indexer.aliasName).build()
        updateAliasesRequestBuilder.actions { it.add(addAction) }
        val updateAliasesRequest = updateAliasesRequestBuilder.build()
        val ack = client.indices().updateAliases(updateAliasesRequest).acknowledged()
        LOG.info("update for alias ${this@Indexer.aliasName} and pointing to $indexName with status: $ack")
        return ack
    }


    fun existsAlias()
        = client.indices().existsAlias(ExistsAliasRequest.Builder().name(this@Indexer.aliasName).build()).value()

    fun getAlias()
        = client.indices().getAlias(GetAliasRequest.Builder().name(this@Indexer.aliasName).build())

    fun createIndex(indexName: String, settings: String, mapping: String): Boolean {
        val mapper = client._transport().jsonpMapper()
        val createIndexRequest = CreateIndexRequest.Builder().index(indexName)
        val settingsParser = mapper.jsonProvider().createParser(StringReader(settings))
        val indexSettings = IndexSettings._DESERIALIZER.deserialize(settingsParser, mapper)
        createIndexRequest.settings(indexSettings)
        val mappingsParser = mapper.jsonProvider().createParser(StringReader(mapping))
        val typeMapping = TypeMapping._DESERIALIZER.deserialize(mappingsParser, mapper)
        createIndexRequest.mappings(typeMapping)
        val ack = client.indices().create(createIndexRequest.build()).acknowledged()!!
        LOG.info("Created $indexName with status: $ack")
        return ack
    }

    fun index(doc: SearchDoc): BulkResponse {
        return index(doc, this@Indexer.aliasName)
    }

    fun index(docs: List<SearchDoc>): BulkResponse {
        LOG.info("indexing ${docs.size} docs to ${this@Indexer.aliasName}")
        return index(docs, this@Indexer.aliasName)
    }

    fun index(doc: SearchDoc, indexName: String): BulkResponse {
        return index(listOf(doc), indexName)
    }

    fun index(docs: List<SearchDoc>, indexName: String): BulkResponse {
        val operations = docs.map { document ->
            BulkOperation.Builder().index(
                IndexOperation.of { it.index(indexName).id(document.id).document(document) }
            ).build()
        }
        val bulkRequest = BulkRequest.Builder()
            .index(indexName)
            .operations(operations)
            .refresh(Refresh.WaitFor)
            .build()
        return try {
            client.bulk(bulkRequest)
        }
        catch (e: Exception) {
            LOG.error("Failed to index $docs to $indexName", e)
            throw e
        }
    }

    fun delete(id: UUID): DeleteResponse {
        return delete(id.toString(), this@Indexer.aliasName)
    }

    fun delete(id: String, indexName: String): DeleteResponse {
        val request = DeleteRequest.Builder().index(indexName).id(id)
        return client.delete(request.build())
    }

    fun indexExists(indexName: String):Boolean =
        client.indices().exists(ExistsRequest.Builder().index(indexName).build()).value()

    fun docCount(): Long = client.count(CountRequest.Builder().index(this@Indexer.aliasName).build()).count()

    private fun initAlias() {
        if (!existsAlias()) {
            LOG.warn("alias ${this@Indexer.aliasName} is not pointing any index")
            val indexName = "${this@Indexer.aliasName}_${LocalDate.now()}"
            LOG.info("Creating index $indexName")
            createIndex(indexName,settings, mapping)
            updateAlias(indexName)
        }
        else {
            LOG.info("Aliases is pointing to ${getAlias().toJsonString()}")
        }
    }
}
