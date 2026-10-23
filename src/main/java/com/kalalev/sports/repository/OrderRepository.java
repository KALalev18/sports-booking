package com.kalalev.sports.repository;

import com.kalalev.sports.model.Order;
import com.kalalev.sports.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {

}
