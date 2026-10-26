package com.kalalev.sports.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccessControlTest {
    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"/facilities", "/login", "/register"})
    void publicPagesOpenWithoutLogin(String url) throws Exception {
        mockMvc.perform(get(url)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin/users", "/admin/fields"})
    void protectedPagesRedirectAnonymousUsersToLogin(String url) throws Exception {
        mockMvc.perform(get(url)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "user@sports.demo", roles = "USER")
    void regularUsersCannotOpenTheAdminPanel() throws Exception {
        mockMvc.perform(get("/admin/users")).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/fields/1/delete").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@sports.demo", roles = "ADMIN")
    void adminsCanOpenTheAdminPanel() throws Exception {
        mockMvc.perform(get("/admin/users")).andExpect(status().isOk());
        mockMvc.perform(get("/admin/fields")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@sports.demo", roles = "ADMIN")
    void formsWithoutACsrfTokenAreRejected() throws Exception {
        mockMvc.perform(post("/admin/fields/1/delete")).andExpect(status().isForbidden());
    }
}
