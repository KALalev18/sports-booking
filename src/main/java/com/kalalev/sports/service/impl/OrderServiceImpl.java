package com.kalalev.sports.service.impl;

import com.kalalev.sports.dto.CartItem;
import com.kalalev.sports.exception.InvalidOrderException;
import com.kalalev.sports.model.Order;
import com.kalalev.sports.model.Product;
import com.kalalev.sports.model.User;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.repository.ProductRepository;
import com.kalalev.sports.service.OrderService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, Clock clock) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.clock = clock;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    @Override
    public List<Order> getOrdersForUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    // Prices come from the database, not from the browser, so a user cannot change what they pay.
    // All lines are saved in one transaction: if any line is invalid, nothing is saved.
    @Override
    @Transactional
    public List<Order> placeOrder(User user, List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("Your cart is empty");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        List<Order> orders = new ArrayList<>();
        for (CartItem item : items) {
            if (item.quantity() < 1 || item.quantity() > MAX_QUANTITY) {
                throw new InvalidOrderException("Quantity must be between 1 and " + MAX_QUANTITY);
            }
            Product product = productRepository
                    .findById(item.productId())
                    .orElseThrow(() -> new InvalidOrderException("A product in your cart is no longer available"));
            BigDecimal total = product.getProductPrice().multiply(BigDecimal.valueOf(item.quantity()));
            orders.add(new Order(user, product, item.quantity(), total, now));
        }
        return orderRepository.saveAll(orders);
    }

    @Override
    public void deleteOrderById(int orderId) {
        orderRepository.deleteById(orderId);
    }
}
