package com.example.spatia.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OrderItem {
    @SerializedName("productId")
    private int productId;

    @SerializedName("quantity")
    private int quantity;

    public OrderItem() {
    }

    public OrderItem(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void incQuantity() {
        this.quantity++;
    }

    public void decQuantity() {
        this.quantity--;
    }
}