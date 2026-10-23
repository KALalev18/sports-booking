package com.kalalev.sports.controller;

import com.kalalev.sports.model.Contact;
import com.kalalev.sports.service.ContactService;
import com.kalalev.sports.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// The About page with its feedback form; sending feedback requires a login
@Controller
public class FeedbackController {
    private final ContactService contactService;
    private final UserService userService;

    public FeedbackController(ContactService contactService, UserService userService) {
        this.contactService = contactService;
        this.userService = userService;
    }

    // Only these fields can be set from the form; ids, user and dates are set on the server
    @InitBinder("contact")
    public void allowFormFields(WebDataBinder binder) {
        binder.setAllowedFields("firstName", "lastName", "phoneNumber", "subject");
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("contact", new Contact());
        return "about";
    }

    @PostMapping("/feedback")
    public String sendFeedback(
            @Valid @ModelAttribute("contact") Contact contact,
            BindingResult bindingResult,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "about";
        }
        contactService.createContact(contact, userService.getUserByEmail(principal.getName()));
        redirectAttributes.addFlashAttribute("success", "Thank you for your feedback!");
        return "redirect:/about";
    }
}
