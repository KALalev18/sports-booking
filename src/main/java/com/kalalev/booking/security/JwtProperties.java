package com.kalalev.booking.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @param secret HMAC key for signing API tokens, at least 32 characters. If empty, a random key is made at startup,
 *     which is fine for a single demo server but logs everyone out on restart.
 * @param tokenLifetime how long an API token is valid
 */
@ConfigurationProperties("booking.jwt")
public record JwtProperties(
        String secret, @DefaultValue("1h") Duration tokenLifetime) {}
