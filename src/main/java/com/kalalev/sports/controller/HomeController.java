package com.kalalev.sports.controller;

import com.kalalev.sports.content.ServiceCatalog;
import com.kalalev.sports.exception.NotFoundException;
import com.kalalev.sports.service.FieldService;
import com.kalalev.sports.service.ProductService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    private final FieldService fieldService;
    private final ProductService productService;

    public HomeController(FieldService fieldService, ProductService productService) {
        this.fieldService = fieldService;
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("services", ServiceCatalog.all());
        return "index";
    }

    @GetMapping("/news")
    public String news() {
        return "news";
    }

    @GetMapping("/services/{slug}")
    public String service(@PathVariable String slug, Model model) {
        model.addAttribute("services", ServiceCatalog.all());
        model.addAttribute("page", ServiceCatalog.find(slug).orElseThrow(() -> new NotFoundException(slug)));
        model.addAttribute("offers", ServiceCatalog.OFFERS);
        return "service";
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "q", defaultValue = "") String query, Model model) {
        String trimmed = query.trim();
        model.addAttribute("query", trimmed);
        if (trimmed.isEmpty()) {
            model.addAttribute("fields", List.of());
            model.addAttribute("products", List.of());
        } else {
            model.addAttribute("fields", fieldService.searchFields(trimmed));
            model.addAttribute("products", productService.searchProducts(trimmed));
        }
        return "search";
    }
}
