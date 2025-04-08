package com.example.spatia.model;

import com.google.firebase.Timestamp;
import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Order {
    @SerializedName("orderId")
    private String orderId;

    @SerializedName("userId")
    private String userId;

    @SerializedName("items")
    private List<OrderItem> orderItems;

    @SerializedName("subtotal")
    private double subtotal;
    
    @SerializedName("deliveryFee")
    private double deliveryFee;

    @SerializedName("totalAmount")
    private double totalAmount;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("paymentStatus")
    private String paymentStatus;

    @SerializedName("orderStatus")
    private String orderStatus;
    
    @SerializedName("orderDate")
    private Timestamp orderDate;
    
    @SerializedName("address")
    private Map<String, Object> address;

    public Order() {
        // Empty constructor required for Firestore
    }

    public Order(String orderId, String userId, double subtotal, double deliveryFee, 
                double totalAmount, String paymentMethod, String paymentStatus, 
                String orderStatus, Timestamp orderDate) {
        this.orderId = orderId;
        this.userId = userId;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public Map<String, Object> getAddress() {
        return address;
    }

    public void setAddress(Map<String, Object> address) {
        this.address = address;
    }

    public String getFormattedOrderDate() {
        if (orderDate == null) return "";
        
        try {
            Date date = orderDate.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "";
        }
    }
}