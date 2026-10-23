package com.kalalev.sports.controller.admin;

import com.kalalev.sports.model.Product;
import com.kalalev.sports.service.ProductService;
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
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    // The id comes from the URL, never from the form
    @InitBinder("product")
    public void allowFormFields(WebDataBinder binder) {
        binder.setAllowedFields("productName", "productPrice", "productDescription", "imagePath");
    }

    @GetMapping("")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }

    @GetMapping("/new")
    public String showCreateProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }

    @PostMapping("/new")
    public String createProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "admin/product-form";
        }
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("success", "Product added.");
        return "redirect:/admin/products";
    }

    @GetMapping("/{productId}/edit")
    public String showEditProductForm(@PathVariable int productId, Model model) {
        model.addAttribute("product", productService.getProductById(productId));
        return "admin/product-form";
    }

    @PostMapping("/{productId}/edit")
    public String updateProduct(
            @PathVariable int productId,
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        product.setProductId(productId);
        if (bindingResult.hasErrors()) {
            return "admin/product-form";
        }
        productService.getProductById(productId);
        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("success", "Product updated.");
        return "redirect:/admin/products";
    }

    @PostMapping("/{productId}/delete")
    public String deleteProduct(@PathVariable int productId, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProductById(productId);
            redirectAttributes.addFlashAttribute("success", "Product deleted.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/products";
    }
}
