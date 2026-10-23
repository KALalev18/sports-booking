package com.kalalev.sports.controller;

import jakarta.validation.Valid;
import com.kalalev.sports.model.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kalalev.sports.service.FieldService;

import java.util.List;

@Controller
@RequestMapping("/fields")
public class FieldController {
    @Autowired
    private FieldService fieldService;

    @GetMapping("")
    public String listFields(Model model) {
        List<Field> fieldList = fieldService.getAllFields();
        model.addAttribute("fieldList", fieldList);
        return "field/list";
    }

    @GetMapping("/new")
    public String showCreateFieldForm(Model model) {
        Field field = new Field();
        model.addAttribute("field", field);
        return "field/create";
    }

    @PostMapping("/new")
    public String createField(@Valid Field field, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "field/create";
        } else {
            fieldService.createField(field);
            return "redirect:/fields";
        }
    }

    @GetMapping("/{fieldId}/edit")
    public String showEditFieldForm(@PathVariable("fieldId") int fieldId, Model model) {
        Field field = fieldService.getFieldById(fieldId);
        model.addAttribute("field", field);
        return "field/edit";
    }

    @PostMapping("/{fieldId}/edit")
    public String updateField(@PathVariable("fieldId") int fieldId, @Valid Field field,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "field/edit";
        } else {
            field.setFieldId(fieldId);
            fieldService.updateField(field);
            return "redirect:/fields";
        }
    }

    @GetMapping("/{fieldId}/delete")
    public String deleteField(@PathVariable("fieldId") int fieldId) {
        fieldService.deleteFieldById(fieldId);
        return "redirect:/fields";
    }
    @PostMapping("/{fieldId}/delete")
    public String delete(@PathVariable("fieldId") int fieldId) {
        fieldService.deleteFieldById(fieldId);
        return "redirect:/fields";
    }
}
