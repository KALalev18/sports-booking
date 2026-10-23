package com.kalalev.sports.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kalalev.sports.dto.RegistrationForm;
import com.kalalev.sports.exception.EmailAlreadyUsedException;
import com.kalalev.sports.model.User;
import com.kalalev.sports.model.UserRole;
import com.kalalev.sports.repository.ConstructionRepository;
import com.kalalev.sports.repository.ContactRepository;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.repository.UserRepository;
import com.kalalev.sports.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ConstructionRepository constructionRepository;

    @Mock
    private RoleService roleService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                orderRepository,
                contactRepository,
                constructionRepository,
                roleService,
                passwordEncoder);
    }

    private static RegistrationForm form(String email) {
        RegistrationForm form = new RegistrationForm();
        form.setFirstName(" Kristian ");
        form.setLastName("Lalev");
        form.setEmail(email);
        form.setPassword("secret123");
        form.setConfirmPassword("secret123");
        return form;
    }

    @Test
    void registerStoresAHashedPasswordAndTheUserRole() {
        UserRole userRole = new UserRole(UserRole.USER);
        when(roleService.getRoleByName(UserRole.USER)).thenReturn(userRole);
        when(userRepository.existsByEmailIgnoreCase("kristian@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.register(form(" Kristian@Example.com "));

        assertThat(user.getEmail()).isEqualTo("kristian@example.com");
        assertThat(user.getFirstName()).isEqualTo("Kristian");
        assertThat(user.getRole()).isSameAs(userRole);
        assertThat(user.getPassword()).isNotEqualTo("secret123").startsWith("$2a$");
        assertThat(passwordEncoder.matches("secret123", user.getPassword())).isTrue();
    }

    @Test
    void registerRejectsAnEmailThatIsAlreadyUsed() {
        when(userRepository.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(form("taken@example.com")))
                .isInstanceOf(EmailAlreadyUsedException.class);
        verify(userRepository, never()).save(any());
    }
}
