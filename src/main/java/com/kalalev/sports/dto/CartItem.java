package com.kalalev.sports.dto;

// One line of the shopping cart as sent by the browser; only the product id and quantity are trusted
public record CartItem(int productId, int quantity) {}
