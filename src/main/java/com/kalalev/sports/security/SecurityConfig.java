package com.kalalev.sports.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // Pages anyone can open; everything else needs a login, and /admin/** needs the ADMIN role
    private static final String[] PUBLIC_PATHS = {
        "/", "/shop", "/facilities", "/news", "/about", "/services/**", "/search",
        "/login", "/register", "/css/**", "/js/**", "/img/**", "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_PATHS)
                        .permitAll()
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form.loginPage("/login")
                        .usernameParameter("email")
                        .successHandler(new LoginSuccessHandler())
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/?logout").permitAll());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
