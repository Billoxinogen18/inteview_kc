apply(plugin = "org.jetbrains.kotlin.plugin.allopen")

dependencies {
    // REST dependencies
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    
    // HTTP client for calling external services
    implementation("io.quarkus:quarkus-rest-client-reactive")
    implementation("io.quarkus:quarkus-rest-client-reactive-jackson")
    
    // Kafka support
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    
    // JSON processing
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Logging
    implementation("io.quarkus:quarkus-logging-json")
    
    // Testing
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("org.apache.camel.quarkus.CamelQuarkusTest")
}
