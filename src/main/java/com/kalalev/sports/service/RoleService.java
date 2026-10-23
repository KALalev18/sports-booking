package com.kalalev.sports.service;

import com.kalalev.sports.model.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleService {
    List<UserRole> getAllRoles();
    UserRole getRoleById(int roleId);
    void updateRole(UserRole userRole);
    void deleteRoleById(int roleId);
}
