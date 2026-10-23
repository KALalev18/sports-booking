package com.kalalev.sports.service;

import com.kalalev.sports.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kalalev.sports.repository.UserRepository;

import java.util.List;

@Service
public interface UserService {
    List<User> getAllUsers();
    User getUserById(int userId);
    void createUser(User user);
    void updateUser(User user);
    void deleteUserById(int userId);
}