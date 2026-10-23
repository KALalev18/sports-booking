package com.kalalev.sports.service;

import com.kalalev.sports.model.UserRole;
import java.util.List;

public interface RoleService {
    List<UserRole> getAllRoles();

    UserRole getRoleByName(String roleName);
}
