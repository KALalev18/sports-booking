package com.kalalev.sports.repository;

import com.kalalev.sports.model.Order;
import com.kalalev.sports.model.Product;
import com.kalalev.sports.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findAllByOrderByOrderDateDesc();

    boolean existsByProduct(Product product);

    void deleteByUser(User user);
}
