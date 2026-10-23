package com.kalalev.sports.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId")
    private int productId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "ProductName", length = 50, nullable = false)
    private String productName;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    @Column(name = "ProductPrice", precision = 10, scale = 2, nullable = false)
    private BigDecimal productPrice;

    @NotBlank
    @Size(max = 255)
    @Column(name = "ProductDescription", length = 255, nullable = false)
    private String productDescription;

    // File name under /img/Equipment/
    @Size(max = 100)
    @Column(name = "ImagePath", length = 100)
    private String imagePath;

    public Product() {}

    public Product(String productName, BigDecimal productPrice, String productDescription, String imagePath) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productDescription = productDescription;
        this.imagePath = imagePath;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
