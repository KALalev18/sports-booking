package com.kalalev.sports.controller;

import com.kalalev.sports.model.Construction;
import com.kalalev.sports.service.ConstructionService;
import com.kalalev.sports.service.FieldService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FacilityController {
    private final FieldService fieldService;
    private final ConstructionService constructionService;
    private final UserService userService;

    public FacilityController(
            FieldService fieldService, ConstructionService constructionService, UserService userService) {
        this.fieldService = fieldService;
        this.constructionService = constructionService;
        this.userService = userService;
    }

    // Only these fields can be set from the form; ids, user and dates are set on the server
    @InitBinder("construction")
    public void allowFormFields(WebDataBinder binder) {
        binder.setAllowedFields("firstName", "lastName", "phoneNumber", "description");
    }

    @GetMapping("/facilities")
    public String facilities(Model model) {
        model.addAttribute("fields", fieldService.getAllFields());
        return "facilities";
    }

    @GetMapping("/facilities/request")
    public String showRequestForm(Model model) {
        model.addAttribute("construction", new Construction());
        model.addAttribute("fields", fieldService.getAllFields());
        return "construction-request";
    }

    @PostMapping("/facilities/request")
    public String submitRequest(
            @Valid @ModelAttribute("construction") Construction construction,
            BindingResult bindingResult,
            @RequestParam(name = "fieldId", required = false) Integer fieldId,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("fields", fieldService.getAllFields());
            model.addAttribute("selectedFieldId", fieldId);
            return "construction-request";
        }
        constructionService.createConstruction(construction, userService.getUserByEmail(principal.getName()), fieldId);
        redirectAttributes.addFlashAttribute("success", "Thank you! Your request was sent. We will call you back.");
        return "redirect:/facilities";
    }
}
