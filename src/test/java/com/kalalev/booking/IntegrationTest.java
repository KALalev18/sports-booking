package com.kalalev.booking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Full application against PostgreSQL in Testcontainers. All integration tests use the same settings, so Spring
 * starts the context once and reuses it. The scheduled expiry job is switched off so it cannot change rows in the
 * middle of a test; tests that need it call it directly.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = "booking.expiry-job.enabled=false")
@Import(TestcontainersConfig.class)
public @interface IntegrationTest {}
