package com.kalalev.sports.service;

import com.kalalev.sports.model.Product;
import com.kalalev.sports.model.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kalalev.sports.repository.ProductRepository;

import java.util.List;

@Service
public interface ProductService {
    List<Product> getAllProducts();
    Product getProductById(int productId);
    void createProduct(Product product);
    void updateProduct(Product product);
    void deleteProductById(int productId);
}
