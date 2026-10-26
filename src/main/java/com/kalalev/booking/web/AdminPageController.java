package com.kalalev.booking.web;

import com.kalalev.booking.domain.Role;
import com.kalalev.booking.service.UserService;
import org.springframework.security.core.Authentication;
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
public class AdminPageController {
    private final UserService userService;

    public AdminPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String users(Model model) {
        model.addAttribute("users", userService.listUsers());
        model.addAttribute("roles", Role.values());
        return "admin-users";
    }

    @PostMapping("/{id}/role")
    public String changeRole(
            @PathVariable long id,
            @RequestParam Role role,
            Authentication authentication,
            RedirectAttributes redirect) {
        if (userService.getByEmail(authentication.getName()).getId() == id) {
            redirect.addFlashAttribute("error", "You cannot change your own role.");
        } else {
            userService.changeRole(id, role);
            redirect.addFlashAttribute("success", "Role updated.");
        }
        return "redirect:/admin/users";
    }
}
