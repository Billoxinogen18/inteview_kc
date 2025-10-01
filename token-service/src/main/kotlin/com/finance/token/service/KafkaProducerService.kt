package com.finance.token.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.finance.token.model.TransactionMessage
import io.smallrye.reactive.messaging.annotations.Channel
import io.smallrye.reactive.messaging.annotations.Emitter
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.slf4j.LoggerFactory

@ApplicationScoped
class KafkaProducerService {
    
    companion object {
        private val logger = LoggerFactory.getLogger(KafkaProducerService::class.java)
    }
    
    @Inject
    @Channel("user-transactions")
    lateinit var kafkaChannel: Emitter<String>
    
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    
    fun publishTransactions(transactionMessage: TransactionMessage) {
        try {
            val messageJson = objectMapper.writeValueAsString(transactionMessage)
            
            // Send message to Kafka channel
            kafkaChannel.send(messageJson)
            
            logger.info("Successfully published {} transactions for user {} to Kafka", 
                transactionMessage.transactions.size, 
                transactionMessage.userId)
                
        } catch (e: Exception) {
            logger.error("Failed to publish transactions to Kafka for user: {}", 
                transactionMessage.userId, e)
            throw RuntimeException("Failed to publish transactions to Kafka", e)
        }
    }
}
