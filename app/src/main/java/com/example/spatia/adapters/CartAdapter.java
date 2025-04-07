package com.example.spatia.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spatia.R;
import com.example.spatia.model.CartItem;
import com.example.spatia.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private Map<Integer, Product> productMap;
    private NumberFormat currencyFormatter;
    private CartItemActionListener actionListener;

    public interface CartItemActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, Map<Integer, Product> productMap, CartItemActionListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.productMap = productMap;
        this.actionListener = listener;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN")); // Using Indian Rupee format
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        Product product = productMap.get(cartItem.getProductId());

        if (product != null) {
            holder.nameTextView.setText(product.getName());
            holder.priceTextView.setText("₹" + product.getPrice());
            holder.quantityTextView.setText(String.valueOf(cartItem.getQuantity()));

            // Load product image using Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .centerCrop()
                    .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_image);
            }

            // Set quantity controls
            holder.incrementButton.setOnClickListener(v -> {
                int newQuantity = cartItem.getQuantity() + 1;
                if (actionListener != null) {
                    actionListener.onQuantityChanged(cartItem, newQuantity);
                }
            });

            holder.decrementButton.setOnClickListener(v -> {
                if (cartItem.getQuantity() > 1) {
                    int newQuantity = cartItem.getQuantity() - 1;
                    if (actionListener != null) {
                        actionListener.onQuantityChanged(cartItem, newQuantity);
                    }
                }
            });

            // Set remove button action
            holder.removeButton.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onItemRemoved(cartItem);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView priceTextView;
        TextView quantityTextView;
        Button incrementButton;
        Button decrementButton;
        ImageButton removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cartItemImage);
            nameTextView = itemView.findViewById(R.id.cartItemName);
            priceTextView = itemView.findViewById(R.id.cartItemPrice);
            quantityTextView = itemView.findViewById(R.id.quantityText);
            incrementButton = itemView.findViewById(R.id.incrementButton);
            decrementButton = itemView.findViewById(R.id.decrementButton);
            removeButton = itemView.findViewById(R.id.removeItemButton);
        }
    }

    public double calculateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                total += product.getPrice() * item.getQuantity();
            }
        }
        return total;
    }
}
