package com.kalalev.sports;

import com.kalalev.sports.model.Order;
import com.kalalev.sports.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.kalalev.sports.repository.OrderRepository;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SportsApplication.class)
public class OrderTesting {
    private OrderRepository orderRepository;
    @Test
    public void testOrder(){
        Order order = new Order();
        order.getProductQuantity(1);
        order.getTotalPrice(19.99);
        orderRepository.save(order);
    }
}
