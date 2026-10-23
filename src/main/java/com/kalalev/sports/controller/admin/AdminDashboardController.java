package com.kalalev.sports.controller.admin;

import com.kalalev.sports.repository.ConstructionRepository;
import com.kalalev.sports.repository.ContactRepository;
import com.kalalev.sports.repository.FieldRepository;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.repository.ProductRepository;
import com.kalalev.sports.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final FieldRepository fieldRepository;
    private final OrderRepository orderRepository;
    private final ContactRepository contactRepository;
    private final ConstructionRepository constructionRepository;

    public AdminDashboardController(
            UserRepository userRepository,
            ProductRepository productRepository,
            FieldRepository fieldRepository,
            OrderRepository orderRepository,
            ContactRepository contactRepository,
            ConstructionRepository constructionRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.fieldRepository = fieldRepository;
        this.orderRepository = orderRepository;
        this.contactRepository = contactRepository;
        this.constructionRepository = constructionRepository;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("fieldCount", fieldRepository.count());
        model.addAttribute("orderCount", orderRepository.count());
        model.addAttribute("contactCount", contactRepository.count());
        model.addAttribute("constructionCount", constructionRepository.count());
        return "admin/dashboard";
    }
}
