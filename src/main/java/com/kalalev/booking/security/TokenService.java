package com.kalalev.booking.security;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    public static final String ISSUER = "sports-booking";
    public static final String ROLES_CLAIM = "roles";

    public record Token(String accessToken, String tokenType, long expiresIn) {}

    private final JwtEncoder encoder;
    private final JwtProperties properties;
    private final Clock clock;

    public TokenService(JwtEncoder encoder, JwtProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    /** A signed token whose subject is the user's email and whose "roles" claim holds their role, e.g. ["MANAGER"]. */
    public Token issue(Authentication authentication) {
        Instant now = clock.instant();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(properties.tokenLifetime()))
                .claim(ROLES_CLAIM, roles)
                .build();
        String jwt = encoder.encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new Token(jwt, "Bearer", properties.tokenLifetime().toSeconds());
    }
}
