package com.kalalev.sports.controller.admin;

import com.kalalev.sports.model.User;
import com.kalalev.sports.service.RoleService;
import com.kalalev.sports.service.UserService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserService userService;
    private final RoleService roleService;

    public AdminUserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", roleService.getAllRoles());
        return "admin/users";
    }

    @PostMapping("/{userId}/role")
    public String updateRole(
            @PathVariable int userId,
            @RequestParam String roleName,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (isCurrentUser(userId, principal)) {
            redirectAttributes.addFlashAttribute("error", "You cannot change your own role.");
            return "redirect:/admin/users";
        }
        userService.updateRole(userId, roleName);
        redirectAttributes.addFlashAttribute("success", "Role updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/delete")
    public String deleteUser(@PathVariable int userId, Principal principal, RedirectAttributes redirectAttributes) {
        if (isCurrentUser(userId, principal)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account here.");
            return "redirect:/admin/users";
        }
        userService.deleteUserById(userId);
        redirectAttributes.addFlashAttribute("success", "User and their orders, feedback and requests deleted.");
        return "redirect:/admin/users";
    }

    // Stops an admin from locking themselves out
    private boolean isCurrentUser(int userId, Principal principal) {
        User current = userService.getUserByEmail(principal.getName());
        return current.getUserId() == userId;
    }
}
