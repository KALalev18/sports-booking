package com.kalalev.sports.service;

import com.kalalev.sports.dto.CartItem;
import com.kalalev.sports.model.Order;
import com.kalalev.sports.model.User;
import java.util.List;

public interface OrderService {
    int MAX_QUANTITY = 99;

    List<Order> getAllOrders();

    List<Order> getOrdersForUser(User user);

    List<Order> placeOrder(User user, List<CartItem> items);

    void deleteOrderById(int orderId);
}
