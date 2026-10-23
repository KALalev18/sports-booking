package com.kalalev.sports.controller;

import jakarta.validation.Valid;
import com.kalalev.sports.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kalalev.sports.service.RoleService;

import java.util.List;

@Controller
@RequestMapping("/roles")
public class UserRoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping("")
    public String listRoles(Model model) {
        List<UserRole> userList = roleService.getAllRoles();
        model.addAttribute("userList", userList);
        return "userList/list";
    }

    @GetMapping("/{roleId}/edit")
    public String showEditRoleForm(@PathVariable("roleId") int roleId, Model model) {
        UserRole userRole = roleService.getRoleById(roleId);
        model.addAttribute("userRole", userRole);
        return "userRole/edit";
    }

    @PostMapping("/{roleId}/edit")
    public String updateRole(@PathVariable("roleId") int roleId, @Valid UserRole userRole,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "userRole/edit";
        } else {
            userRole.setRoleId(roleId);
            roleService.updateRole(userRole);
            return "redirect:/users";
        }
    }
}