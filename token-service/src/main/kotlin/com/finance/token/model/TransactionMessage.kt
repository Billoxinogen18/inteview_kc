package com.finance.token.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionMessage(
    val userId: String,
    val transactions: List<TransactionData>,
    val timestamp: String,
    val tokenInfo: TokenInfo
)

data class TransactionData(
    val id: String,
    val accountId: String,
    val amount: BigDecimal,
    val currency: String,
    val type: String,
    val description: String,
    val merchantName: String?,
    val category: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val timestamp: LocalDateTime,
    val status: String,
    val reference: String,
    val balance: BigDecimal?
)

data class TokenInfo(
    val subject: String?,
    val issuer: String?,
    @JsonProperty("expires_at")
    val expiresAt: Long?
)
