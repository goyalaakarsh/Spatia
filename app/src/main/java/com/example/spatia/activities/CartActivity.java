package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spatia.R;
import com.example.spatia.adapters.CartAdapter;
import com.example.spatia.adapters.ProductAdapter;
import com.example.spatia.model.CartItem;
import com.example.spatia.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemActionListener {

    private static final String TAG = "CartActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private RecyclerView recyclerView;
    private RecyclerView recommendedRecyclerView;
    private CartAdapter adapter;
    private ProductAdapter recommendedProductsAdapter;
    private List<CartItem> cartItems;
    private List<Product> recommendedProducts;
    private Map<Integer, Product> productMap;
    private TextView totalPriceTextView;
    private TextView emptyCartTextView;
    private TextView swipeToCheckoutTextView;
    private ProgressBar progressBar;

    private NumberFormat currencyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login to view your cart", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        recyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.cartTotalPrice);
        emptyCartTextView = findViewById(R.id.emptyCartText);
        swipeToCheckoutTextView = findViewById(R.id.swipeToCheckout);
        progressBar = findViewById(R.id.cartProgressBar);

        cartItems = new ArrayList<>();
        recommendedProducts = new ArrayList<>();
        productMap = new HashMap<>();
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN")); // Using Indian Rupee format

        adapter = new CartAdapter(this, cartItems, productMap, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeToCheckoutTextView.setOnClickListener(v -> processCheckout());

        loadCartItems();
        loadRecommendedProducts();
    }

    private void loadCartItems() {
        showProgress(true);

        String userId = currentUser.getUid();
        db.collection("carts").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");
                            if (items != null && !items.isEmpty()) {
                                cartItems.clear();
                                for (Map<String, Object> item : items) {
                                    int productId = ((Long) item.get("productId")).intValue();
                                    int quantity = ((Long) item.get("quantity")).intValue();
                                    cartItems.add(new CartItem(productId, quantity));
                                }
                                
                                loadProductDetails();
                            } else {
                                showEmptyCartView();
                                showProgress(false);
                            }
                        } else {
                            showEmptyCartView();
                            showProgress(false);
                        }
                    } else {
                        Log.w(TAG, "Error getting cart items", task.getException());
                        Toast.makeText(CartActivity.this, 
                                "Failed to load cart: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        showProgress(false);
                    }
                });
    }

    private void loadProductDetails() {
        if (cartItems.isEmpty()) {
            showEmptyCartView();
            showProgress(false);
            return;
        }

        List<Integer> productIds = new ArrayList<>();
        for (CartItem item : cartItems) {
            productIds.add(item.getProductId());
        }

        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        productMap.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            if (productIds.contains(product.getId())) {
                                productMap.put(product.getId(), product);
                            }
                        }

                        if (!productMap.isEmpty()) {
                            updateCartUI();
                        } else {
                            showEmptyCartView();
                        }
                    } else {
                        Log.w(TAG, "Error getting products", task.getException());
                        Toast.makeText(CartActivity.this,
                                "Failed to load products: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadRecommendedProducts() {
        db.collection("products")
            .whereEqualTo("category", "Sofa")
            .limit(5)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                recommendedProducts.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Product product = document.toObject(Product.class);
                    recommendedProducts.add(product);
                }
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error loading recommended products", e);
            });
    }

    private void updateCartUI() {
        if (cartItems.isEmpty()) {
            showEmptyCartView();
            return;
        }

        emptyCartTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();

        double totalPrice = calculateTotalPrice();
        totalPriceTextView.setText(currencyFormatter.format(totalPrice));
    }

    private double calculateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                total += product.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    private void showEmptyCartView() {
        recyclerView.setVisibility(View.GONE);
        emptyCartTextView.setVisibility(View.VISIBLE);
        totalPriceTextView.setText(currencyFormatter.format(0));
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        // Update local cart item quantity
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId() == item.getProductId()) {
                cartItem.setQuantity(newQuantity);
                break;
            }
        }

        // Update UI
        adapter.notifyDataSetChanged();
        double totalPrice = calculateTotalPrice();
        totalPriceTextView.setText(currencyFormatter.format(totalPrice));

        updateCartInFirestore();
    }

    @Override
    public void onItemRemoved(CartItem item) {
        // Remove item from local cart
        cartItems.remove(item);

        if (cartItems.isEmpty()) {
            showEmptyCartView();
        } else {
            adapter.notifyDataSetChanged();
            double totalPrice = calculateTotalPrice();
            totalPriceTextView.setText(currencyFormatter.format(totalPrice));
        }

        updateCartInFirestore();
        
        Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show();
    }

    private void updateCartInFirestore() {
        if (currentUser == null) return;
        
        String userId = currentUser.getUid();
        
        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("productId", cartItem.getProductId());
            item.put("quantity", cartItem.getQuantity());
            items.add(item);
        }
        
        Map<String, Object> cartData = new HashMap<>();
        cartData.put("userId", userId);
        cartData.put("items", items);

        db.collection("carts").document(userId)
                .set(cartData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Cart updated successfully"))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error updating cart", e);
                    Toast.makeText(CartActivity.this, 
                            "Failed to update cart: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void processCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to checkout activity
        Intent intent = new Intent(this, CheckoutActivity.class);
        startActivity(intent);
    }
}
