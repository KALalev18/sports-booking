package com.kalalev.sports.service;

import com.kalalev.sports.dto.RegistrationForm;
import com.kalalev.sports.model.User;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(int userId);

    User getUserByEmail(String email);

    User register(RegistrationForm form);

    void updateRole(int userId, String roleName);

    void deleteUserById(int userId);
}
