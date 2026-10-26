package com.kalalev.booking.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class LoginSuccessHandlerTest {

    @ParameterizedTest
    @ValueSource(strings = {"/", "/bookings", "/?city=Sofia&sport=TENNIS"})
    void acceptsPathsOnThisSite(String path) {
        assertThat(LoginSuccessHandler.isLocalPath(path)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"https://evil.example", "//evil.example", "/\\evil.example", "bookings"})
    void rejectsEverythingElse(String path) {
        assertThat(LoginSuccessHandler.isLocalPath(path)).isFalse();
    }
}
