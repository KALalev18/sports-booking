package com.kalalev.sports.controller;

import jakarta.validation.Valid;
import com.kalalev.sports.model.Construction;
import com.kalalev.sports.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kalalev.sports.service.ConstructionService;

import java.util.List;

@Controller
@RequestMapping("/constructions")
public class ConstructionController {
    @Autowired
    private ConstructionService constructionService;

    @GetMapping("")
    public String listUsers(Model model) {
        List<Construction> constructionList = constructionService.getAllConstructions();
        model.addAttribute("constructionList", constructionList);
        return "construction/list";
    }

    @GetMapping("/new")
    public String showCreateUserForm(Model model) {
        User construction = new User();
        model.addAttribute("construction", construction);
        return "construction/create";
    }

    @PostMapping("/new")
    public String createUser(@Valid Construction construction, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "construction/create";
        } else {
            constructionService.createConstruction(construction);
            return "redirect:/constructions";
        }
    }

    @GetMapping("/{constructionId}/edit")
    public String showEditUserForm(@PathVariable("constructionId") int constructionId, Model model) {
        Construction construction = constructionService.getConstructionById(constructionId);
        model.addAttribute("construction", construction);
        return "construction/edit";
    }

    @PostMapping("/{constructionId}/edit")
    public String updateUser(@PathVariable("constructionId") int constructionId, @Valid Construction construction,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "construction/edit";
        } else {
            construction.setConstructionId(constructionId);
            constructionService.updateConstruction(construction);
            return "redirect:/constructions";
        }
    }

    @GetMapping("/{constructionId}/delete")
    public String deleteUser(@PathVariable("constructionId") int constructionId) {
        constructionService.deleteConstructionById(constructionId);
        return "redirect:/constructions";
    }
    @PostMapping("/{constructionId}/delete")
    public String delete(@PathVariable("constructionId") int constructionId) {
        constructionService.deleteConstructionById(constructionId);
        return "redirect:/constructions";
    }
}
