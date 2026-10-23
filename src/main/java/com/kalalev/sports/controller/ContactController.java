package com.kalalev.sports.controller;

import jakarta.validation.Valid;
import com.kalalev.sports.model.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kalalev.sports.service.ContactService;

import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactController {
    @Autowired
    private ContactService contactService;

    @GetMapping("")
    public String listContacts(Model model) {
        List<Contact> contactList = contactService.getAllContacts();
        model.addAttribute("contactList", contactList);
        return "contact/list";
    }

    @GetMapping("/new")
    public String showCreateContactForm(Model model) {
        Contact contact = new Contact();
        model.addAttribute("contact", contact);
        return "contact/create";
    }

    @PostMapping("/new")
    public String createContact(@Valid Contact contact, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "contact/create";
        } else {
            contactService.createContact(contact);
            return "redirect:/contacts";
        }
    }

    @GetMapping("/{contactId}/edit")
    public String showEditContactForm(@PathVariable("contactId") int contactId, Model model) {
        Contact contact = contactService.getContactById(contactId);
        model.addAttribute("contact", contact);
        return "contact/edit";
    }

    @PostMapping("/{contactId}/edit")
    public String updateContact(@PathVariable("contactId") int contactId, @Valid Contact contact,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "contact/edit";
        } else {
            contact.setContactId(contactId);
            contactService.updateContact(contact);
            return "redirect:/contacts";
        }
    }

    @GetMapping("/{contactId}/delete")
    public String deleteContact(@PathVariable("contactId") int contactId) {
        contactService.deleteContactById(contactId);
        return "redirect:/contacts";
    }
    @PostMapping("/{contactId}/delete")
    public String delete(@PathVariable("contactId") int contactId) {
        contactService.deleteContactById(contactId);
        return "redirect:/contacts";
    }
}
