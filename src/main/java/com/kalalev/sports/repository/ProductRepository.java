package com.kalalev.sports.repository;

import com.kalalev.sports.model.Product;
import com.kalalev.sports.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
