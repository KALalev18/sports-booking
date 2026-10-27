package com.kalalev.booking.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Signs and checks API tokens with HMAC-SHA256. One key is enough because this app both issues and verifies the
 * tokens; with a separate login service you would use an RSA key pair and publish the public key instead.
 */
@Configuration(proxyBeanMethods = false)
public class JwtConfig {
    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    @Bean
    SecretKey jwtSigningKey(JwtProperties properties) {
        String secret = properties.secret();
        if (secret == null || secret.isBlank()) {
            log.warn("booking.jwt.secret is not set: using a random key, so API tokens stop working on restart");
            byte[] random = new byte[32];
            new SecureRandom().nextBytes(random);
            return new SecretKeySpec(random, "HmacSHA256");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("booking.jwt.secret must be at least 32 bytes for HS256");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
    }

    /** Checks the signature, that the token is not expired (by the app's clock) and that this app issued it. */
    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSigningKey, Clock clock) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator expiry = new JwtTimestampValidator();
        expiry.setClock(clock);
        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(expiry, new JwtIssuerValidator(TokenService.ISSUER)));
        return decoder;
    }
}
