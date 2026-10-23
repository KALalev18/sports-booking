package com.kalalev.sports.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.kalalev.sports.model.Construction;
import com.kalalev.sports.model.Order;
import com.kalalev.sports.model.User;
import com.kalalev.sports.repository.ConstructionRepository;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ConstructionRepository constructionRepository;

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

    @Test
    @WithMockUser(username = "user@sports.demo", roles = "USER")
    void checkoutIgnoresAnyPriceSentByTheBrowser() throws Exception {
        mockMvc.perform(post("/orders")
                        .with(csrf())
                        .param("productId", "1")
                        .param("quantity", "3")
                        .param("price", "0.01")
                        .param("totalPrice", "0.01"))
                .andExpect(redirectedUrl("/orders"));

        User user = userRepository.findByEmailIgnoreCase("user@sports.demo").orElseThrow();
        List<Order> orders = orderRepository.findByUserOrderByOrderDateDesc(user);
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getProduct().getProductName()).isEqualTo("Football");
        assertThat(orders.get(0).getTotalPrice()).isEqualByComparingTo("59.97");
    }

    @Test
    @WithMockUser(username = "user@sports.demo", roles = "USER")
    void constructionRequestCannotOverwriteAnotherRow() throws Exception {
        mockMvc.perform(post("/facilities/request")
                        .with(csrf())
                        .param("firstName", "Demo")
                        .param("lastName", "User")
                        .param("phoneNumber", "+359 888 123 456")
                        .param("description", "A new pitch in Sozopol")
                        .param("fieldId", "2")
                        .param("constructionId", "999")
                        .param("user.userId", "1"))
                .andExpect(redirectedUrl("/facilities"));

        List<Construction> requests = constructionRepository.findAll();
        assertThat(requests).hasSize(1);
        Construction request = requests.get(0);
        assertThat(request.getConstructionId()).isNotEqualTo(999);
        assertThat(request.getUser().getEmail()).isEqualTo("user@sports.demo");
        assertThat(request.getField().getFieldName()).isEqualTo("Ivan Vazov School Sports Ground");
    }
}
