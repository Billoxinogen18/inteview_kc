package com.finance.token.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.finance.token.client.KeycloakClient
import com.finance.token.client.TransactionServiceClient
import com.finance.token.model.TransactionMessage
import com.finance.token.model.TransactionData
import com.finance.token.model.TokenInfo
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.Instant

@ApplicationScoped
class TokenService {
    
    companion object {
        private val logger = LoggerFactory.getLogger(TokenService::class.java)
    }
    
    @Inject
    @RestClient
    lateinit var keycloakClient: KeycloakClient
    
    @Inject
    @RestClient
    lateinit var transactionServiceClient: TransactionServiceClient
    
    @Inject
    lateinit var kafkaProducerService: KafkaProducerService
    
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    
    fun processOAuthFlow(code: String): Map<String, Any> {
        return try {
            logger.info("Starting OAuth flow for code: {}", code)
            
            // Step 1: Exchange authorization code for access token
            val tokenResponse = exchangeTokenWithKeycloak(code)
            logger.info("Successfully exchanged code for access token")
            
            // Step 2: Fetch transactions using the access token
            val transactions = fetchTransactionsFromService(tokenResponse.accessToken)
            logger.info("Successfully fetched {} transactions", transactions.size)
            
            // Step 3: Create transaction message for Kafka
            val transactionMessage = createTransactionMessage(transactions, tokenResponse)
            
            // Step 4: Publish to Kafka
            kafkaProducerService.publishTransactions(transactionMessage)
            
            logger.info("Successfully completed OAuth flow for user: {}", transactionMessage.userId)
            
            mapOf(
                "status" to "success",
                "message" to "Transactions processed and published to Kafka",
                "userId" to transactionMessage.userId,
                "transactionCount" to transactionMessage.transactions.size,
                "timestamp" to Instant.now().toString()
            )
            
        } catch (e: Exception) {
            logger.error("Error processing OAuth flow", e)
            mapOf(
                "status" to "error",
                "message" to "Failed to process OAuth flow: ${e.message}",
                "timestamp" to Instant.now().toString()
            )
        }
    }
    
    private fun exchangeTokenWithKeycloak(code: String) = try {
        keycloakClient.exchangeToken(
            grantType = "authorization_code",
            clientId = "finance-client",
            clientSecret = "finance-secret",
            code = code,
            redirectUri = "http://localhost:8081/token"
        )
    } catch (e: Exception) {
        logger.error("Failed to exchange token with Keycloak", e)
        throw RuntimeException("OAuth token exchange failed", e)
    }
    
    private fun fetchTransactionsFromService(accessToken: String): List<Map<String, Any>> {
        return try {
            val response = transactionServiceClient.getTransactions(
                auth = "Bearer $accessToken",
                limit = 20
            )
            
            @Suppress("UNCHECKED_CAST")
            response["transactions"] as? List<Map<String, Any>> ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to fetch transactions from service", e)
            throw RuntimeException("Transaction fetching failed", e)
        }
    }
    
    private fun createTransactionMessage(transactions: List<Map<String, Any>>, tokenResponse: com.finance.token.model.OAuthTokenResponse): TransactionMessage {
        val transactionData = transactions.map { transactionMap ->
            TransactionData(
                id = transactionMap["id"] as String,
                accountId = transactionMap["accountId"] as String,
                amount = BigDecimal.valueOf((transactionMap["amount"] as Number).toDouble()),
                currency = transactionMap["currency"] as String,
                type = transactionMap["type"] as String,
                description = transactionMap["description"] as String,
                merchantName = transactionMap["merchantName"] as String?,
                category = transactionMap["category"] as String,
                timestamp = java.time.LocalDateTime.parse(transactionMap["timestamp"] as String),
                status = transactionMap["status"] as String,
                reference = transactionMap["reference"] as String,
                balance = (transactionMap["balance"] as Number?)?.let { BigDecimal.valueOf(it.toDouble()) }
            )
        }
        
        // Extract user ID from first transaction or use a default
        val userId = transactions.firstOrNull()?.get("accountId")?.toString()?.substringAfter("ACC-")?.substringBefore("-") ?: "unknown"
        
        return TransactionMessage(
            userId = userId,
            transactions = transactionData,
            timestamp = Instant.now().toString(),
            tokenInfo = TokenInfo(
                subject = "user-from-token",
                issuer = "http://localhost:8080/realms/finance-app",
                expiresAt = System.currentTimeMillis() / 1000 + tokenResponse.expiresIn
            )
        )
    }
}
