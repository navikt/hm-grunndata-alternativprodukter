package no.nav.hm.grunndata.alternativprodukter.oebs

import jakarta.inject.Singleton

@Singleton
class OebsWarehouseService( private val azureBody: AzureBody,
                            private val oebsClient: OebsClient,
                           private val azureAdClient: AzureAdClient) {

    var tokenCached: TokenCached = TokenCached(
        azureAdToken = AzureAdTokenResponse(
            access_token = "",
            expires_in = 0,
            token_type = ""
        ),
        expires = -1
    )

    suspend fun getWarehouseStockSingle(hmsArtnr: String) : List<OebsStockResponse> {
        return oebsClient.getWarehouseStock(hmsArtnr, "Bearer ${getAccessToken()}")
    }

    suspend fun getWarehouseStockForCentral(hmsnrs: Set<String>, enhetnr: String): List<OebsStockResponse> {
        return oebsClient.getWarehouseStockForCentral(
            enhetnr = enhetnr, hmsnrs = HmsnrsDTO(hmsnrs = hmsnrs),
            authorization = "Bearer ${getAccessToken()}"
        )
    }

    suspend fun getWarehouseStocks(hmsnrs: Set<String>): List<OebsStockResponse> {
        return oebsClient.getWarehouseStocks(
            hmsnrs = HmsnrsDTO(hmsnrs = hmsnrs),
            authorization = "Bearer ${getAccessToken()}"
        )
    }

    suspend fun getAccessToken(): String {
        LOG.debug("current token expires at: ${tokenCached.expires}, current time: ${System.currentTimeMillis()}")
        if (tokenCached.isExpired()) {
            LOG.debug("Token expired, fetching new token")
            tokenCached = TokenCached(azureAdToken = azureAdClient.getToken(azureBody))
        }
        return tokenCached.azureAdToken.access_token
    }

    companion object {
        private val LOG = org.slf4j.LoggerFactory.getLogger(OebsWarehouseService::class.java)
    }
}