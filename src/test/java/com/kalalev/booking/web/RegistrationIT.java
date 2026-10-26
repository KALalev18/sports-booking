package com.kalalev.booking.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kalalev.booking.TestcontainersConfig;
import com.kalalev.booking.domain.AppUser;
import com.kalalev.booking.domain.Role;
import com.kalalev.booking.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
@Transactional
class RegistrationIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void registersACustomerWithAHashedPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Ana Ivanova")
                        .param("email", " Ana@Example.com ")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(redirectedUrl("/login"));

        AppUser user = userRepository.findByEmail("ana@example.com").orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.getPasswordHash()).startsWith("$2a$").doesNotContain("secret123");
    }

    @Test
    void showsErrorsForMismatchedPasswordsAndATakenEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Ana Ivanova")
                        .param("email", "ana@example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "different"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form", "passwordConfirmed"));

        mockMvc.perform(post("/register")
                .with(csrf())
                .param("fullName", "Ana Ivanova")
                .param("email", "ana@example.com")
                .param("password", "secret123")
                .param("confirmPassword", "secret123"));
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "Ana Again")
                        .param("email", "ANA@example.com")
                        .param("password", "secret123")
                        .param("confirmPassword", "secret123"))
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("form", "email"));
    }

    @Test
    void publicPagesOpenAndProtectedPagesRedirectToLogin() throws Exception {
        mockMvc.perform(get("/facilities")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/users")).andExpect(status().is3xxRedirection());
    }
}
