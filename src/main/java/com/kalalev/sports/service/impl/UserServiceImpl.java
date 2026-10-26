package com.kalalev.sports.service.impl;

import com.kalalev.sports.dto.RegistrationForm;
import com.kalalev.sports.exception.EmailAlreadyUsedException;
import com.kalalev.sports.exception.NotFoundException;
import com.kalalev.sports.model.User;
import com.kalalev.sports.model.UserRole;
import com.kalalev.sports.repository.UserRepository;
import com.kalalev.sports.service.RoleService;
import com.kalalev.sports.service.UserService;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(int userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("User " + email + " not found"));
    }

    @Override
    @Transactional
    public User register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        User user = new User(
                form.getFirstName().trim(),
                form.getLastName().trim(),
                email,
                passwordEncoder.encode(form.getPassword()),
                roleService.getRoleByName(UserRole.USER));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateRole(int userId, String roleName) {
        User user = getUserById(userId);
        user.setRole(roleService.getRoleByName(roleName));
    }

    @Override
    @Transactional
    public void deleteUserById(int userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}
