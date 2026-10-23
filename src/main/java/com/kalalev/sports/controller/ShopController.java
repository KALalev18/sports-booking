package com.kalalev.sports.controller;

import com.kalalev.sports.dto.CartItem;
import com.kalalev.sports.exception.InvalidOrderException;
import com.kalalev.sports.model.User;
import com.kalalev.sports.service.OrderService;
import com.kalalev.sports.service.ProductService;
import com.kalalev.sports.service.UserService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShopController {
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;

    public ShopController(ProductService productService, OrderService orderService, UserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/shop")
    public String shop(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "shop";
    }

    // The cart lives in the browser; checkout sends matching lists of product ids and quantities
    @PostMapping("/orders")
    public String placeOrder(
            @RequestParam(name = "productId", required = false) List<Integer> productIds,
            @RequestParam(name = "quantity", required = false) List<Integer> quantities,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = userService.getUserByEmail(principal.getName());
        try {
            orderService.placeOrder(user, toCartItems(productIds, quantities));
        } catch (InvalidOrderException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/shop";
        }
        redirectAttributes.addFlashAttribute("success", "Thank you! Your order has been placed.");
        redirectAttributes.addFlashAttribute("orderPlaced", true);
        return "redirect:/orders";
    }

    @GetMapping("/orders")
    public String myOrders(Principal principal, Model model) {
        User user = userService.getUserByEmail(principal.getName());
        model.addAttribute("orders", orderService.getOrdersForUser(user));
        return "orders";
    }

    private static List<CartItem> toCartItems(List<Integer> productIds, List<Integer> quantities) {
        if (productIds == null || quantities == null || productIds.size() != quantities.size()) {
            throw new InvalidOrderException("Your cart could not be read. Please try again.");
        }
        List<CartItem> items = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            items.add(new CartItem(productIds.get(i), quantities.get(i)));
        }
        return items;
    }
}
