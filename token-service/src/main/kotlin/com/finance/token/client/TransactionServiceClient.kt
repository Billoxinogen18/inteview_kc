package com.finance.token.client

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient
@Path("/api/transactions")
interface TransactionServiceClient {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getTransactions(
        @HeaderParam("Authorization") auth: String,
        @QueryParam("limit") @DefaultValue("20") limit: Int
    ): Map<String, Any>
}
