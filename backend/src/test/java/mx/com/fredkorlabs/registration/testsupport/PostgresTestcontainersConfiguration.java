package mx.com.fredkorlabs.registration.testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * ADR-0018: one PostgreSQL 17 container per test run, on a random host port, shared by every
 * Spring test context. Flyway migrates the empty database; the container is removed when the run ends.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestcontainersConfiguration {

    // Every cached Spring test context keeps its own connection pool (10 by default) against this one
    // container, and Spring caches up to 32 contexts: the default limit of 100 connections is not enough.
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(DockerImageName.parse("postgres:17"))
        .withCommand("postgres", "-c", "max_connections=400");

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return POSTGRES;
    }
}
