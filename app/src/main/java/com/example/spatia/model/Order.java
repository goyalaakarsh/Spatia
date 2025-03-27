package com.example.spatia.model;

import com.google.gson.annotations.SerializedName;
import com.example.spatia.model.OrderItem;

import java.util.List;

public class Order {
    @SerializedName("userId")
    private long userId;

    @SerializedName("orderItems")
    private List<OrderItem> orderItems;

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("paymentMode")
    private String paymentMode;

    @SerializedName("paymentStatus")
    private boolean paymentStatus;

    @SerializedName("orderStatus")
    private String orderStatus;

    public Order() {
    }

    public Order(long userId, List<OrderItem> orderItems, double totalPrice, String paymentMode, boolean paymentStatus) {
        this.userId = userId;
        this.orderItems = orderItems;
        this.totalPrice = totalPrice;
        this.paymentMode = paymentMode;
        this.paymentStatus = paymentStatus;
        this.orderStatus = "Not Shipped";
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }


}