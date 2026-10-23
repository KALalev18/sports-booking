package com.kalalev.sports.service;

import com.kalalev.sports.model.Product;
import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(int productId);

    List<Product> searchProducts(String query);

    void saveProduct(Product product);

    void deleteProductById(int productId);
}
