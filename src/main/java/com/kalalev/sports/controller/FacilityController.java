package com.kalalev.sports.controller;

import com.kalalev.sports.service.FieldService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FacilityController {
    private final FieldService fieldService;

    public FacilityController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    @GetMapping("/facilities")
    public String facilities(Model model) {
        model.addAttribute("fields", fieldService.getAllFields());
        return "facilities";
    }
}
