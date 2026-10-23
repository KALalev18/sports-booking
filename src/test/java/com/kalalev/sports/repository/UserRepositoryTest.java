package com.kalalev.sports.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kalalev.sports.model.User;
import com.kalalev.sports.model.UserRole;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

// Runs against an in-memory H2 database created from the entity mappings
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository roleRepository;

    private UserRole userRole;

    @BeforeEach
    void setUp() {
        userRole = roleRepository.save(new UserRole(UserRole.USER));
    }

    @Test
    void savesAndFindsAUserByEmailIgnoringCase() {
        userRepository.save(new User("Kristian", "Lalev", "kristian@example.com", "hash", userRole));

        assertThat(userRepository.findByEmailIgnoreCase("KRISTIAN@example.com"))
                .get()
                .extracting(User::getLastName)
                .isEqualTo("Lalev");
    }

    @Test
    void rejectsAUserWithoutTheRequiredFields() {
        User user = new User();
        user.setLastName("Georgiev");

        assertThatThrownBy(() -> userRepository.saveAndFlush(user)).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    void rejectsASecondUserWithTheSameEmail() {
        userRepository.saveAndFlush(new User("Ana", "Ivanova", "ana@example.com", "hash", userRole));

        assertThatThrownBy(() ->
                        userRepository.saveAndFlush(new User("Ana", "Petrova", "ana@example.com", "hash", userRole)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
