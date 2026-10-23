package com.kalalev.sports.service;

import com.kalalev.sports.model.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {
    List<Order> getAllOrders();
    Order getOrderById(int orderId);
    void createOrder(Order order);
    void updateOrder(Order order);
    void deleteOrderById(int orderId);
}
