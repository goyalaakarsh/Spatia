package com.example.spatia.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spatia.R;
import com.example.spatia.activities.ProductDetailActivity;
import com.example.spatia.model.CartItem;
import com.example.spatia.model.Product;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        
        holder.productName.setText(product.getName());
        holder.productPrice.setText("₹" + product.getPrice());
        holder.productCategory.setText(product.getCategory());
        
        // Load product image using Glide
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .centerCrop()
                .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.placeholder_image);
        }
        
        // Set click listener to open product details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            context.startActivity(intent);
        });
        
        // Set click listener for Add to Cart button
        holder.addToCartButton.setOnClickListener(v -> {
            addToCart(product);
        });
    }
    
    private void addToCart(Product product) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            Toast.makeText(context, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = currentUser.getUid();
        
        // First check if the cart already exists
        db.collection("carts").document(userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    
                    if (document.exists()) {
                        // Cart exists, update it
                        updateExistingCart(userId, product);
                    } else {
                        // Create new cart
                        createNewCart(userId, product);
                    }
                } else {
                    Toast.makeText(context, "Failed to add item to cart", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void updateExistingCart(String userId, Product product) {
        db.collection("carts").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                List<Map<String, Object>> cartItems = (List<Map<String, Object>>) documentSnapshot.get("items");
                
                if (cartItems == null) {
                    cartItems = new ArrayList<>();
                }
                
                // Check if item already exists in cart
                boolean itemExists = false;
                for (Map<String, Object> item : cartItems) {
                    int productId = ((Long) item.get("productId")).intValue();
                    
                    if (productId == product.getId()) {
                        // Item exists, increment quantity
                        int quantity = ((Long) item.get("quantity")).intValue();
                        item.put("quantity", quantity + 1);
                        itemExists = true;
                        break;
                    }
                }
                
                // If item doesn't exist, add it
                if (!itemExists) {
                    Map<String, Object> newItem = new HashMap<>();
                    newItem.put("productId", product.getId());
                    newItem.put("quantity", 1);
                    cartItems.add(newItem);
                }
                
                // Update cart in Firestore
                Map<String, Object> updatedCart = new HashMap<>();
                updatedCart.put("userId", userId);
                updatedCart.put("items", cartItems);
                
                db.collection("carts").document(userId)
                    .set(updatedCart)
                    .addOnSuccessListener(aVoid -> 
                        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> 
                        Toast.makeText(context, "Failed to add item to cart", Toast.LENGTH_SHORT).show());
            })
            .addOnFailureListener(e -> 
                Toast.makeText(context, "Failed to add item to cart", Toast.LENGTH_SHORT).show());
    }
    
    private void createNewCart(String userId, Product product) {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("productId", product.getId());
        item.put("quantity", 1);
        items.add(item);
        
        Map<String, Object> cart = new HashMap<>();
        cart.put("userId", userId);
        cart.put("items", items);
        
        db.collection("carts").document(userId)
            .set(cart)
            .addOnSuccessListener(aVoid -> 
                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> 
                Toast.makeText(context, "Failed to add item to cart", Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
    
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productCategory;
        MaterialButton addToCartButton;
        
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCategory = itemView.findViewById(R.id.productCategory);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
    }
}
