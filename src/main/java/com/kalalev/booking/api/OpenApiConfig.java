package com.kalalev.booking.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Swagger UI at /swagger-ui.html. Get a token from POST /api/v1/auth/token, then use the Authorize button. */
@Configuration(proxyBeanMethods = false)
@OpenAPIDefinition(
        info =
                @Info(
                        title = "Sports Booking API",
                        version = "v1",
                        description = "Book sports courts by the hour. Times are local to the facility."))
@SecurityScheme(name = OpenApiConfig.BEARER, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
    public static final String BEARER = "bearer-jwt";
}
