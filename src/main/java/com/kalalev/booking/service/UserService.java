package com.kalalev.booking.service;

import com.kalalev.booking.domain.AppUser;
import com.kalalev.booking.domain.Role;
import com.kalalev.booking.repository.AppUserRepository;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    /** Creates a customer account. The password is stored only as a BCrypt hash. */
    @Transactional
    public AppUser register(String email, String password, String fullName) {
        String normalized = normalize(email);
        if (userRepository.existsByEmail(normalized)) {
            throw new EmailTakenException(normalized);
        }
        return userRepository.save(new AppUser(
                normalized, passwordEncoder.encode(password), fullName.trim(), Role.CUSTOMER, clock.instant()));
    }

    @Transactional(readOnly = true)
    public AppUser getByEmail(String email) {
        return userRepository
                .findByEmail(normalize(email))
                .orElseThrow(() -> new NotFoundException("No user " + email));
    }

    @Transactional(readOnly = true)
    public List<AppUser> listUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void changeRole(long userId, Role role) {
        userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("No user " + userId))
                .setRole(role);
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
