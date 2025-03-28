package com.example.spatia.model;

import com.google.gson.annotations.SerializedName;
import com.example.spatia.model.CartItem;

import java.util.List;

public class Cart {
    @SerializedName("userId")
    private long userId;

    @SerializedName("items")
    List<CartItem> items;

    public Cart() {
    }

    public Cart(long userId, List<CartItem> items) {
        this.userId = userId;
        this.items = items;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public List<CartItem> getItems() {
        return items;
    }
    public void setItems(List<CartItem> items) {
        this.items = items;
    }
    public void addOrUpdateItem(int productId, int quantity) {
        for (CartItem item : items) {
            if (item.getProductId() == productId) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(productId, quantity));
    }
    public void removeItem(CartItem item) {
        items.remove(item);
    }
}