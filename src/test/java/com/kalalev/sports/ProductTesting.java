package com.kalalev.sports;

import com.kalalev.sports.model.Product;
import com.kalalev.sports.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.kalalev.sports.repository.ProductRepository;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SportsApplication.class)
public class ProductTesting {
    private ProductRepository productRepository;
    @Test
    public void testProduct(){
        Product product = new Product();
        product.getProductName("Topka");
        product.getProductPrice("19.99");
        product.getProductDescription("Krugla forma");
        productRepository.save(product);
    }
}
