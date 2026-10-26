package com.kalalev.sports.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kalalev.sports.model.User;
import com.kalalev.sports.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// End-to-end flows through the controllers, services and the H2 database seeded by DataSeeder.
// Each test runs in a transaction that is rolled back afterwards.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserFlowTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registrationCreatesAUserWithAHashedPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("firstName", "Ana")
                        .param("lastName", "Ivanova")
                        .param("email", "ana@example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(redirectedUrl("/login"));

        User user = userRepository.findByEmailIgnoreCase("ana@example.com").orElseThrow();
        assertThat(user.getPassword()).startsWith("$2a$");
        assertThat(user.isAdmin()).isFalse();
    }

    @Test
    void registrationShowsErrorsForMismatchedPasswordsAndTakenEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("firstName", "Demo")
                        .param("lastName", "User")
                        .param("email", "user@sports.demo")
                        .param("password", "secret123")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form", "passwordConfirmed"));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("firstName", "Demo")
                        .param("lastName", "User")
                        .param("email", "user@sports.demo")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form", "email"));
    }
}
