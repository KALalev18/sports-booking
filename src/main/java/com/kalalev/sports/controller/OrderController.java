package com.kalalev.sports.controller;

import jakarta.validation.Valid;
import com.kalalev.sports.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.kalalev.sports.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("")
    public String listOrders(Model model) {
        List<Order> orderList = orderService.getAllOrders();
        model.addAttribute("orderList", orderList);
        return "order/list";
    }

    @GetMapping("/new")
    public String showCreateOrderForm(Model model) {
        Order order = new Order();
        model.addAttribute("order", order);
        return "order/create";
    }

    @PostMapping("/new")
    public String createOrder(@Valid Order order, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "order/create";
        } else {
            orderService.createOrder(order);
            return "redirect:/orders";
        }
    }

    @GetMapping("/{orderId}/edit")
    public String showEditOrderForm(@PathVariable("orderId") int orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "order/edit";
    }

    @PostMapping("/{orderId}/edit")
    public String updateOrder(@PathVariable("orderId") int orderId, @Valid Order order,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "order/edit";
        } else {
            order.setOrderId(orderId);
            orderService.updateOrder(order);
            return "redirect:/orders";
        }
    }

    @GetMapping("/{orderId}/delete")
    public String deleteOrder(@PathVariable("orderId") int orderId) {
        orderService.deleteOrderById(orderId);
        return "redirect:/orders";
    }
    @PostMapping("/{orderId}/delete")
    public String delete(@PathVariable("orderId") int orderId) {
        orderService.deleteOrderById(orderId);
        return "redirect:/orders";
    }
}
