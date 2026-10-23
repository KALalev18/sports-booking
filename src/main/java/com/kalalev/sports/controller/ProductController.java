package com.kalalev.sports.controller;

import jakarta.validation.Valid;
import com.kalalev.sports.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kalalev.sports.service.ProductService;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("")
    public String listProducts(Model model) {
        List<Product> productList = productService.getAllProducts();
        model.addAttribute("productList", productList);
        return "productList/list";
    }

    @GetMapping("/new")
    public String showCreateProductForm(Model model) {
        Product product = new Product();
        model.addAttribute("product", product);
        return "product/create";
    }

    @PostMapping("/new")
    public String createProduct(@Valid Product product, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "product/create";
        } else {
            productService.createProduct(product);
            return "redirect:/products";
        }
    }

    @GetMapping("/{productId}/edit")
    public String showEditUserForm(@PathVariable("productId") int productId, Model model) {
        Product product = productService.getProductById(productId);
        model.addAttribute("product", product);
        return "product/edit";
    }

    @PostMapping("/{productId}/edit")
    public String updateProduct(@PathVariable("productId") int productId, @Valid Product product,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "product/edit";
        } else {
            product.setProductId(productId);
            productService.updateProduct(product);
            return "redirect:/products";
        }
    }

    @GetMapping("/{productId}/delete")
    public String deleteProduct(@PathVariable("userId") int productId) {
        productService.deleteProductById(productId);
        return "redirect:/users";
    }
    @PostMapping("/{productId}/delete")
    public String delete(@PathVariable("productId") int productId) {
        productService.deleteProductById(productId);
        return "redirect:/users";
    }
}
