package com.kalalev.sports.controller.admin;

import com.kalalev.sports.model.Field;
import com.kalalev.sports.service.FieldService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/fields")
public class AdminFieldController {
    private final FieldService fieldService;

    public AdminFieldController(FieldService fieldService) {
        this.fieldService = fieldService;
    }

    // The id comes from the URL, never from the form
    @InitBinder("field")
    public void allowFormFields(WebDataBinder binder) {
        binder.setAllowedFields(
                "fieldName",
                "fieldLocation",
                "workingDays",
                "startedWorkingTime",
                "finishedWorkingTime",
                "priceByHour",
                "imageUrl",
                "mapUrl");
    }

    @GetMapping("")
    public String listFields(Model model) {
        model.addAttribute("fields", fieldService.getAllFields());
        return "admin/fields";
    }

    @GetMapping("/new")
    public String showCreateFieldForm(Model model) {
        model.addAttribute("field", new Field());
        return "admin/field-form";
    }

    @PostMapping("/new")
    public String createField(
            @Valid @ModelAttribute("field") Field field,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/field-form";
        }
        fieldService.saveField(field);
        redirectAttributes.addFlashAttribute("success", "Facility added.");
        return "redirect:/admin/fields";
    }

    @GetMapping("/{fieldId}/edit")
    public String showEditFieldForm(@PathVariable int fieldId, Model model) {
        model.addAttribute("field", fieldService.getFieldById(fieldId));
        return "admin/field-form";
    }

    @PostMapping("/{fieldId}/edit")
    public String updateField(
            @PathVariable int fieldId,
            @Valid @ModelAttribute("field") Field field,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        field.setFieldId(fieldId);
        if (bindingResult.hasErrors()) {
            return "admin/field-form";
        }
        fieldService.getFieldById(fieldId);
        fieldService.saveField(field);
        redirectAttributes.addFlashAttribute("success", "Facility updated.");
        return "redirect:/admin/fields";
    }

    @PostMapping("/{fieldId}/delete")
    public String deleteField(@PathVariable int fieldId, RedirectAttributes redirectAttributes) {
        fieldService.deleteFieldById(fieldId);
        redirectAttributes.addFlashAttribute("success", "Facility deleted.");
        return "redirect:/admin/fields";
    }
}
