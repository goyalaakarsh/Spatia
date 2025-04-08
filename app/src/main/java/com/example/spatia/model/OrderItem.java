package com.example.spatia.model;

import com.google.gson.annotations.SerializedName;

public class OrderItem {
    @SerializedName("productId")
    private int productId;

    @SerializedName("quantity")
    private int quantity;
    
    @SerializedName("productName")
    private String productName;
    
    @SerializedName("productPrice")
    private double productPrice;
    
    @SerializedName("productImage")
    private String productImage;

    public OrderItem() {
        // Empty constructor required for Firestore
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
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public double getProductPrice() {
        return productPrice;
    }
    
    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }
    
    public String getProductImage() {
        return productImage;
    }
    
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public void incQuantity() {
        this.quantity++;
    }

    public void decQuantity() {
        this.quantity--;
    }
}