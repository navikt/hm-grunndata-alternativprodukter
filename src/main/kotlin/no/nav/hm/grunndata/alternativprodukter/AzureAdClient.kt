package no.nav.hm.grunndata.alternativprodukter

import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import jakarta.inject.Singleton

@Client("\${azure.endpoint}")
interface AzureAdClient {
    @Post(value = "/", produces = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun getToken(@Body authBody: AzureBody): AzureAdTokenResponse
}

data class AzureAdTokenResponse(
    val access_token: String,
    val expires_in: Long,
    val token_type: String,
)

@Singleton
class AzureBody(
    @Value("\${azure.client.id}")
    val client_id: String,
    @Value("\${azure.client.secret}")
    val client_secret: String,
    @Value("\${azure.grant.type}")
    val grant_type: String,
    @Value("\${azure.scope}")
    val scope: String,
)