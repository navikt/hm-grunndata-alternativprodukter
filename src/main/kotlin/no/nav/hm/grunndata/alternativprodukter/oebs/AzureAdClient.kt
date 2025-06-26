package no.nav.hm.grunndata.alternativprodukter.oebs

import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton

@Client("\${azure.endpoint}")
interface AzureAdClient {
    @Post(value = "/", produces = [MediaType.APPLICATION_FORM_URLENCODED], consumes = ["application/json"])
    @SingleResult
    suspend fun getToken(@Body authBody: AzureBody): AzureAdTokenResponse
}

@Serdeable
data class AzureAdTokenResponse(
    val access_token: String,
    val expires_in: Long,
    val token_type: String,
)

@Serdeable
data class TokenCached (
    val azureAdToken : AzureAdTokenResponse,
    val expires: Long = System.currentTimeMillis() + azureAdToken.expires_in * 1000
) {

    fun isExpired(): Boolean {
        return System.currentTimeMillis() >= expires
    }
}

@Singleton
@Serdeable
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