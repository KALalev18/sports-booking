package com.kalalev.booking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Two filter chains:
 *
 * <ul>
 *   <li>{@code /api/**}: stateless. Clients send {@code Authorization: Bearer <JWT>}; no session, no cookies, so no
 *       CSRF token is needed.
 *   <li>Everything else: the Thymeleaf site, with a login form, a session cookie and CSRF protection.
 * </ul>
 *
 * Both use the same accounts and roles. Rules about which facility a manager may touch are checked on the service
 * methods with {@code @PreAuthorize}, so they apply to both the API and the site.
 */
@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PAGES = {
        "/",
        "/facilities",
        "/login",
        "/register",
        "/css/**",
        "/js/**",
        "/error",
        "/actuator/health",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/token")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/facilities", "/api/v1/availability")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(rolesFromToken())));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_PAGES)
                        .permitAll()
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/manage/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form.loginPage("/login")
                        .usernameParameter("email")
                        .successHandler(new LoginSuccessHandler())
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/?logout").permitAll());
        return http.build();
    }

    /** Turns the token's "roles" claim, e.g. ["MANAGER"], into the authority ROLE_MANAGER. */
    private static JwtAuthenticationConverter rolesFromToken() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(TokenService.ROLES_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    /** Checks email and password for the API token endpoint. */
    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
