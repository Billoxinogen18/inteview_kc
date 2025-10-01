package com.finance.token.client

import com.finance.token.model.OAuthTokenResponse
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient
@Path("/realms/finance-app/protocol/openid-connect")
interface KeycloakClient {
    
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    fun exchangeToken(
        @FormParam("grant_type") grantType: String,
        @FormParam("client_id") clientId: String,
        @FormParam("client_secret") clientSecret: String,
        @FormParam("code") code: String,
        @FormParam("redirect_uri") redirectUri: String
    ): OAuthTokenResponse
}
