package com.kalalev.booking;

import java.time.Instant;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * A real PostgreSQL 17 in Docker for integration tests, plus a movable clock. The beans are shared by every test
 * that imports this configuration, because Spring caches the application context between test classes.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    /** Monday 1 June 2026, 06:00 UTC (09:00 in Sofia). Tests book slots on the following days. */
    public static final Instant TEST_START = Instant.parse("2026-06-01T06:00:00Z");

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }

    @Bean
    @Primary
    MutableClock testClock() {
        return new MutableClock(TEST_START);
    }

    @Bean
    TestData testData(JdbcTemplate jdbc) {
        return new TestData(jdbc);
    }
}
