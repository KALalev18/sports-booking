package com.kalalev.booking.api;

import com.kalalev.booking.security.TokenService;
import com.kalalev.booking.security.TokenService.Token;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthApi {

    public record TokenRequest(
            @NotBlank String email, @NotBlank String password) {}

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthApi(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    @Operation(summary = "Exchange email and password for a bearer token")
    public Token token(@Valid @RequestBody TokenRequest request) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        return tokenService.issue(authentication);
    }
}
