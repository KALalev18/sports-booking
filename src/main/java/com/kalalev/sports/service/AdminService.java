package com.kalalev.sports.service;

import com.kalalev.sports.model.Admin;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdminService {
    List<Admin> getAllAdmins();
    Admin getAdminById(int adminId);
    void createAdmin(Admin admin);
    void updateAdmin(Admin admin);
    void deleteAdminById(int adminId);
}
