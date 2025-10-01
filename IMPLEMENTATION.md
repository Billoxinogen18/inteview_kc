# Token Service Implementation

## Overview

This implementation provides a complete **token-service** using Apache Camel and Kotlin that handles OAuth token exchange, fetches transactions from the transactions-service, and publishes them to Kafka.

## Architecture

The token service follows a microservices architecture with the following components:

### 1. OAuth Flow Implementation
- **Endpoint**: `GET /token?code=<auth_code>`
- **Process**: 
  1. Receives authorization code from Keycloak redirect
  2. Exchanges authorization code for JWT access token with Keycloak
  3. Uses the JWT token to fetch transactions from transactions-service
  4. Publishes transaction data to Kafka topic

### 2. Camel Routes
- **Main Route**: `oauth-token-exchange` - Handles the complete OAuth flow
- **Health Check**: `/health` - Service health monitoring
- **Error Handling**: Comprehensive error handling with proper HTTP status codes

### 3. Processors
- **OAuthTokenProcessor**: Prepares OAuth token exchange request
- **TokenResponseProcessor**: Processes token response from Keycloak
- **TransactionFetchProcessor**: Processes transaction data for Kafka publishing

## Project Structure

```
token-service/
├── src/main/kotlin/com/finance/token/
│   ├── routes/
│   │   └── TokenServiceRoutes.kt          # Main Camel routes
│   ├── processors/
│   │   ├── OAuthTokenProcessor.kt        # OAuth token exchange
│   │   ├── TokenResponseProcessor.kt      # Token response handling
│   │   └── TransactionFetchProcessor.kt   # Transaction data processing
│   └── model/
│       ├── OAuthTokenRequest.kt           # OAuth request/response models
│       └── TransactionMessage.kt          # Kafka message models
├── src/main/resources/
│   └── application.properties             # Service configuration
├── src/test/kotlin/com/finance/token/
│   └── processors/                        # Unit tests
└── build.gradle.kts                       # Dependencies and build config
```

## Dependencies

The token service includes the following key dependencies:

- **Apache Camel Quarkus**: Core Camel framework
- **Camel HTTP4**: HTTP client for calling external services
- **Camel Kafka**: Kafka producer for message publishing
- **Camel Jackson**: JSON processing
- **Camel REST**: REST endpoint configuration

## Configuration

### Application Properties
- **Port**: 8081
- **Kafka**: localhost:9092
- **Transactions Service**: localhost:8082
- **Keycloak**: localhost:8080

### Environment Variables
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker addresses
- `TRANSACTIONS_SERVICE_URL`: Transactions service URL
- `KEYCLOAK_URL`: Keycloak server URL

## API Endpoints

### 1. OAuth Token Exchange
```
GET /token?code=<authorization_code>
```

**Response (Success)**:
```json
{
    "status": "success",
    "message": "Transactions processed and published to Kafka",
    "userId": "testuser",
    "transactionCount": 20,
    "timestamp": "2024-01-01T12:00:00"
}
```

**Response (Error)**:
```json
{
    "status": "error",
    "message": "Failed to exchange authorization code for access token",
    "error": "HTTP 400"
}
```

### 2. Health Check
```
GET /health
```

**Response**:
```json
{
    "status": "UP",
    "service": "Token Service",
    "timestamp": "2024-01-01T12:00:00",
    "port": "8081"
}
```

## Kafka Integration

### Topic Configuration
- **Topic Name**: `user-transactions`
- **Message Format**: JSON
- **Partitioning**: Single partition (configurable)

### Message Structure
```json
{
    "userId": "testuser",
    "transactions": [
        {
            "id": "txn-123",
            "accountId": "ACC-testuser-001",
            "amount": 100.50,
            "currency": "EUR",
            "type": "DEBIT",
            "description": "Test transaction",
            "merchantName": "Test Store",
            "category": "Shopping",
            "timestamp": "2024-01-01T12:00:00",
            "status": "COMPLETED",
            "reference": "REF-123",
            "balance": 1000.00
        }
    ],
    "timestamp": "2024-01-01T12:00:00",
    "tokenInfo": {
        "subject": "testuser",
        "issuer": "http://localhost:8080/realms/finance-app",
        "expiresAt": 1234567890
    }
}
```

## Error Handling

The service includes comprehensive error handling:

1. **OAuth Token Exchange Failures**: HTTP 400/401 responses from Keycloak
2. **Transaction Service Failures**: HTTP 500/503 responses from transactions-service
3. **Kafka Publishing Failures**: Connection and serialization errors
4. **Network Timeouts**: Configurable timeouts for HTTP calls

## Testing

### Unit Tests
- **OAuthTokenProcessorTest**: Tests OAuth request preparation
- **TransactionFetchProcessorTest**: Tests transaction data processing

### Integration Testing
To test the complete flow:

1. Start all services:
```bash
# Start Kafka
docker-compose up -d

# Start transactions service
./gradlew :transactions-service:quarkusDev

# Start token service
./gradlew :token-service:quarkusDev
```

2. Test OAuth flow:
```
http://localhost:8080/realms/finance-app/protocol/openid-connect/auth?client_id=finance-client&redirect_uri=http://localhost:8081/token&response_type=code&scope=openid
```

3. Verify Kafka messages:
```bash
# Check Kafka topics
docker exec -it kafka kafka-topics --bootstrap-server localhost:9092 --list

# Consume messages
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic user-transactions --from-beginning
```

## Monitoring and Logging

### Logging Configuration
- **Level**: INFO
- **Format**: Structured JSON logging
- **Console Output**: Enabled for development

### Key Log Messages
- OAuth code reception
- Token exchange success/failure
- Transaction fetching results
- Kafka publishing status
- Error conditions and stack traces

## Performance Considerations

1. **HTTP Timeouts**: 5s connection, 10s socket timeout
2. **Kafka Batching**: Configurable batch size for message publishing
3. **Memory Usage**: Stream caching enabled for large payloads
4. **Concurrent Requests**: Handled by Quarkus HTTP layer

## Security

1. **JWT Token Validation**: Tokens are validated by transactions-service
2. **HTTPS Support**: Configurable for production environments
3. **CORS Configuration**: Restricted to specific origins
4. **Input Validation**: Authorization code validation

## Deployment

### Docker Support
The service can be containerized using Quarkus native compilation:

```bash
./gradlew :token-service:build -Dquarkus.package.type=native
```

### Production Configuration
- Update `application.properties` for production URLs
- Configure Kafka cluster settings
- Set up proper logging and monitoring
- Configure security settings

## Troubleshooting

### Common Issues

1. **Kafka Connection Failed**
   - Check if Kafka is running: `docker-compose ps`
   - Verify Kafka port 9092 is accessible

2. **Transactions Service Unavailable**
   - Check if transactions-service is running on port 8082
   - Verify service health: `curl http://localhost:8082/api/transactions/health`

3. **Keycloak Connection Issues**
   - Verify Keycloak is running on port 8080
   - Check realm and client configuration

4. **OAuth Token Exchange Failures**
   - Verify client credentials in OAuthTokenProcessor
   - Check redirect URI configuration

### Debug Mode
Enable debug logging by setting:
```properties
quarkus.log.level=DEBUG
camel.main.tracing=true
```

## Future Enhancements

1. **Metrics Collection**: Add Prometheus metrics
2. **Circuit Breaker**: Implement resilience patterns
3. **Message Validation**: Add schema validation for Kafka messages
4. **Batch Processing**: Support for bulk transaction processing
5. **Audit Logging**: Comprehensive audit trail for compliance
