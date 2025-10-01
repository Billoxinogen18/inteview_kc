package com.finance.token.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OAuthTokenRequest(
    @JsonProperty("grant_type")
    val grantType: String = "authorization_code",
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String,
    val code: String,
    @JsonProperty("redirect_uri")
    val redirectUri: String
)

data class OAuthTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String = "Bearer",
    @JsonProperty("expires_in")
    val expiresIn: Long,
    @JsonProperty("refresh_token")
    val refreshToken: String? = null,
    val scope: String? = null
)

data class TokenExchangeRequest(
    val code: String,
    @JsonProperty("redirect_uri")
    val redirectUri: String = "http://localhost:8081/token"
)
