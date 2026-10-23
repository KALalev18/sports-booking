package com.kalalev.sports.service.impl;

import com.kalalev.sports.model.Admin;
import com.kalalev.sports.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kalalev.sports.repository.AdminRepository;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.service.AdminService;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private AdminRepository adminRepository;
    @Override
    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    @Override
    public Admin getAdminById(int adminId) {
        Optional<Admin> admin = adminRepository.findById(adminId);
        return admin.orElse(null);
    }

    @Override
    public void createAdmin(Admin admin) {
        adminRepository.save(admin);
    }

    @Override
    public void updateAdmin(Admin admin) {
        adminRepository.save(admin);
    }

    @Override
    public void deleteAdminById(int adminId) {
        adminRepository.deleteById(adminId);
    }
}
