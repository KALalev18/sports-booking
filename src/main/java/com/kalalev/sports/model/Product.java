package com.kalalev.sports.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId", columnDefinition = "int", nullable = false)
    private int productId;
    @Column(name = "ProductName", columnDefinition = "nvarchar(50)", nullable = false)
    private String productName;
    @Column(name = "ProductPrice", columnDefinition = "decimal(10, 2)", nullable = false)
    private float productPrice;
    @Column(name = "ProductDescription", columnDefinition = "varchar(255)", nullable = false)
    private String productDescription;

    public Product(int productId, String productName, float productPrice, String productDescription) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productDescription = productDescription;
    }

    public Product(){}

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName(String topka) {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public float getProductPrice(String number) {
        return productPrice;
    }

    public void setProductPrice(float productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductDescription(String kruglaForma) {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
}
