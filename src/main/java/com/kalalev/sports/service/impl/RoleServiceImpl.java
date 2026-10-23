package com.kalalev.sports.service.impl;

import com.kalalev.sports.model.User;
import com.kalalev.sports.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kalalev.sports.repository.UserRepository;
import com.kalalev.sports.repository.UserRoleRepository;
import com.kalalev.sports.service.RoleService;

import java.util.List;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Override
    public List<UserRole> getAllRoles() {
        return userRoleRepository.findAll();
    }

    @Override
    public UserRole getRoleById(int roleId) {
        Optional<UserRole> userRole = userRoleRepository.findById(roleId);
        return userRole.orElse(null);
    }

    @Override
    public void updateRole(UserRole userRole) {
        userRoleRepository.save(userRole);
    }

    @Override
    public void deleteRoleById(int roleId) {
        userRoleRepository.deleteById(roleId);
    }
}
