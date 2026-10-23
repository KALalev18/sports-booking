package com.kalalev.sports.service.impl;

import com.kalalev.sports.exception.NotFoundException;
import com.kalalev.sports.model.Product;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.repository.ProductRepository;
import com.kalalev.sports.service.ProductService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public ProductServiceImpl(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(int productId) {
        return productRepository
                .findById(productId)
                .orElseThrow(() -> new NotFoundException("Product " + productId + " not found"));
    }

    @Override
    public List<Product> searchProducts(String query) {
        return productRepository.findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(
                query, query);
    }

    @Override
    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    // Products that appear in orders are kept so order history stays intact
    @Override
    public void deleteProductById(int productId) {
        Product product = getProductById(productId);
        if (orderRepository.existsByProduct(product)) {
            throw new IllegalStateException(product.getProductName() + " has orders and cannot be deleted");
        }
        productRepository.delete(product);
    }
}
