package com.kalalev.booking.web;

import com.kalalev.booking.service.EmailTakenException;
import com.kalalev.booking.service.UserService;
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

    // POST /login is handled by Spring Security
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
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
            userService.register(form.getEmail(), form.getPassword(), form.getFullName());
        } catch (EmailTakenException e) {
            bindingResult.rejectValue("email", "taken", "An account with this email already exists");
            return "register";
        }
        redirectAttributes.addFlashAttribute("success", "Account created. You can log in now.");
        return "redirect:/login";
    }
}
