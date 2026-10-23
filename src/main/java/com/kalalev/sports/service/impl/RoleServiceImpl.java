package com.kalalev.sports.service.impl;

import com.kalalev.sports.exception.NotFoundException;
import com.kalalev.sports.model.UserRole;
import com.kalalev.sports.repository.UserRoleRepository;
import com.kalalev.sports.service.RoleService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    private final UserRoleRepository userRoleRepository;

    public RoleServiceImpl(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public List<UserRole> getAllRoles() {
        return userRoleRepository.findAll();
    }

    @Override
    public UserRole getRoleByName(String roleName) {
        return userRoleRepository
                .findByRoleName(roleName)
                .orElseThrow(() -> new NotFoundException("Role " + roleName + " not found"));
    }
}
