package com.kalalev.sports.controller.admin;

import com.kalalev.sports.service.ConstructionService;
import com.kalalev.sports.service.ContactService;
import com.kalalev.sports.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Orders, feedback and construction requests: created by users, reviewed and removed by admins
@Controller
@RequestMapping("/admin")
public class AdminSubmissionController {
    private final OrderService orderService;
    private final ContactService contactService;
    private final ConstructionService constructionService;

    public AdminSubmissionController(
            OrderService orderService, ContactService contactService, ConstructionService constructionService) {
        this.orderService = orderService;
        this.contactService = contactService;
        this.constructionService = constructionService;
    }

    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }

    @PostMapping("/orders/{orderId}/delete")
    public String deleteOrder(@PathVariable int orderId, RedirectAttributes redirectAttributes) {
        orderService.deleteOrderById(orderId);
        redirectAttributes.addFlashAttribute("success", "Order deleted.");
        return "redirect:/admin/orders";
    }

    @GetMapping("/contacts")
    public String listContacts(Model model) {
        model.addAttribute("contacts", contactService.getAllContacts());
        return "admin/contacts";
    }

    @PostMapping("/contacts/{contactId}/delete")
    public String deleteContact(@PathVariable int contactId, RedirectAttributes redirectAttributes) {
        contactService.deleteContactById(contactId);
        redirectAttributes.addFlashAttribute("success", "Feedback deleted.");
        return "redirect:/admin/contacts";
    }

    @GetMapping("/constructions")
    public String listConstructions(Model model) {
        model.addAttribute("constructions", constructionService.getAllConstructions());
        return "admin/constructions";
    }

    @PostMapping("/constructions/{constructionId}/delete")
    public String deleteConstruction(@PathVariable int constructionId, RedirectAttributes redirectAttributes) {
        constructionService.deleteConstructionById(constructionId);
        redirectAttributes.addFlashAttribute("success", "Request deleted.");
        return "redirect:/admin/constructions";
    }
}
