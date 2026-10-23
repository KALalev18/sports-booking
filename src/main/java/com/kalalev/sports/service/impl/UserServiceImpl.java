package com.kalalev.sports.service.impl;

import com.kalalev.sports.dto.RegistrationForm;
import com.kalalev.sports.exception.EmailAlreadyUsedException;
import com.kalalev.sports.exception.NotFoundException;
import com.kalalev.sports.model.User;
import com.kalalev.sports.model.UserRole;
import com.kalalev.sports.repository.ConstructionRepository;
import com.kalalev.sports.repository.ContactRepository;
import com.kalalev.sports.repository.OrderRepository;
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
    private final OrderRepository orderRepository;
    private final ContactRepository contactRepository;
    private final ConstructionRepository constructionRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            OrderRepository orderRepository,
            ContactRepository contactRepository,
            ConstructionRepository constructionRepository,
            RoleService roleService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.contactRepository = contactRepository;
        this.constructionRepository = constructionRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(int userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User " + userId + " not found"));
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

    // Removes the user's orders, feedback and requests first, since they reference the user
    @Override
    @Transactional
    public void deleteUserById(int userId) {
        User user = getUserById(userId);
        orderRepository.deleteByUser(user);
        contactRepository.deleteByUser(user);
        constructionRepository.deleteByUser(user);
        userRepository.delete(user);
    }
}
