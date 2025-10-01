package com.finance.token.resource

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.slf4j.LoggerFactory

@Path("/health")
class HealthResource {
    
    companion object {
        private val logger = LoggerFactory.getLogger(HealthResource::class.java)
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun health(): Response {
        logger.info("Health check requested")
        
        return Response.ok(mapOf(
            "status" to "UP",
            "service" to "Token Service",
            "port" to "8081",
            "timestamp" to System.currentTimeMillis()
        )).build()
    }
}
