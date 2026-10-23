package com.kalalev.sports.security;

import com.kalalev.sports.model.User;
import com.kalalev.sports.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Lets Spring Security log users in by email, using the Users table
@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().getRoleName())
                .build();
    }
}
