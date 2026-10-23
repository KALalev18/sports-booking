package com.kalalev.sports.controller;

import com.kalalev.sports.dto.RegistrationForm;
import com.kalalev.sports.exception.EmailAlreadyUsedException;
import com.kalalev.sports.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // The POST /login request is handled by Spring Security
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("form") RegistrationForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(form);
        } catch (EmailAlreadyUsedException e) {
            bindingResult.rejectValue("email", "duplicate", "An account with this email already exists");
            return "register";
        }
        redirectAttributes.addFlashAttribute("success", "Account created. You can log in now.");
        return "redirect:/login";
    }
}
