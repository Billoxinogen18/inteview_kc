package com.finance.token.resource

import com.finance.token.service.TokenService
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.slf4j.LoggerFactory

@Path("/token")
class TokenResource {
    
    companion object {
        private val logger = LoggerFactory.getLogger(TokenResource::class.java)
    }
    
    @Inject
    lateinit var tokenService: TokenService
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getToken(@QueryParam("code") code: String?): Response {
        logger.info("Received OAuth code: {}", code)
        
        return try {
            if (code.isNullOrBlank()) {
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("error" to "Authorization code is required"))
                    .build()
            } else {
                // Process the complete OAuth flow
                val result = tokenService.processOAuthFlow(code)
                
                if (result["status"] == "success") {
                    Response.ok(result).build()
                } else {
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(result)
                        .build()
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing token request", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf(
                    "status" to "error",
                    "message" to "Internal server error: ${e.message}"
                ))
                .build()
        }
    }
}
