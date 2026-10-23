package com.kalalev.sports.controller;

import jakarta.validation.Valid;
import com.kalalev.sports.model.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kalalev.sports.service.AdminService;

import java.util.List;

@Controller
@RequestMapping("/admins")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("")
    public String listAdmins(Model model) {
        List<Admin> adminList = adminService.getAllAdmins();
        model.addAttribute("adminList", adminList);
        return "admin/list";
    }

    @GetMapping("/new")
    public String showCreateAdminForm(Model model) {
        Admin admin = new Admin();
        model.addAttribute("admin", admin);
        return "admin/create";
    }

    @PostMapping("/new")
    public String createAdmin(@Valid Admin admin, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/create";
        } else {
            adminService.createAdmin(admin);
            return "redirect:/admins";
        }
    }

    @GetMapping("/{adminId}/edit")
    public String showEditAdminForm(@PathVariable("adminId") int adminId, Model model) {
        Admin admin = adminService.getAdminById(adminId);
        model.addAttribute("admin", admin);
        return "admin/edit";
    }

    @PostMapping("/{adminId}/edit")
    public String updateAdmin(@PathVariable("adminId") int adminId, @Valid Admin admin,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/edit";
        } else {
            admin.setAdminId(adminId);
            adminService.updateAdmin(admin);
            return "redirect:/admins";
        }
    }

    @GetMapping("/{adminId}/delete")
    public String deleteAdmin(@PathVariable("adminId") int adminId) {
        adminService.deleteAdminById(adminId);
        return "redirect:/admins";
    }
    @PostMapping("/{adminId}/delete")
    public String delete(@PathVariable("adminId") int adminId) {
        adminService.deleteAdminById(adminId);
        return "redirect:/admins";
    }
}
