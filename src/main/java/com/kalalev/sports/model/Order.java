package com.kalalev.sports.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId", columnDefinition = "int", nullable = false)
    private int orderId;
    @OneToOne
    @JoinColumn(name = "UserId")
    @Column(name = "UserId", columnDefinition = "datetime", nullable = false)
    private User userId;
    @ManyToMany
    @JoinColumn(name = "ProductId")
    @Column(name = "ProductId", columnDefinition = "nvarchar(100)", nullable = false)
    private Set<Product> productId;
    @Column(name = "ProductQuantity", columnDefinition = "nvarchar(100)", nullable = false)
    private int productQuantity;
    @Column(name = "TotalPrice", columnDefinition = "float", nullable = false)
    private int totalPrice;

    public Order(int orderId, User userId, Set<Product> productId, int productQuantity, int totalPrice) {
        this.orderId = orderId;
        this.userId = userId;
        this.productId = productId;
        this.productQuantity = productQuantity;
        this.totalPrice = totalPrice;
    }
    public Order(){}

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public Set<Product> getProductId() {
        return productId;
    }

    public void setProductId(Set<Product> productId) {
        this.productId = productId;
    }

    public int getProductQuantity(int i) {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public int getTotalPrice(double v) {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }
}
