package com.example.spatia.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spatia.R;
import com.example.spatia.adapters.CartAdapter;
import com.example.spatia.model.CartItem;
import com.example.spatia.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    private CartAdapter adapter;
    private List<CartItem> cartItems;
    private Map<Integer, Product> productMap;
    private TextView totalPriceTextView;
    private TextView emptyCartTextView;
    private Button checkoutButton;
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
            finish();
            return;
        }

        recyclerView = findViewById(R.id.cartRecyclerView);
        totalPriceTextView = findViewById(R.id.cartTotalPrice);
        emptyCartTextView = findViewById(R.id.emptyCartText);
        //checkoutButton = findViewById(R.id.checkoutButton);
        progressBar = findViewById(R.id.cartProgressBar);

        cartItems = new ArrayList<>();
        productMap = new HashMap<>();
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        adapter = new CartAdapter(this, cartItems, productMap, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        checkoutButton.setOnClickListener(v -> processCheckout());

        loadCartItems();
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
                            if (items != null) {
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

        // Fetch products from Firestore
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

    private void updateCartUI() {
        if (cartItems.isEmpty()) {
            showEmptyCartView();
            return;
        }

        emptyCartTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
        
        // Update total price
        double totalPrice = adapter.calculateTotalPrice();
        totalPriceTextView.setText(currencyFormatter.format(totalPrice));
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
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId() == item.getProductId()) {
                cartItem.setQuantity(newQuantity);
                break;
            }
        }

        adapter.notifyDataSetChanged();
        double totalPrice = adapter.calculateTotalPrice();
        totalPriceTextView.setText(currencyFormatter.format(totalPrice));

        updateCartInFirestore();
    }

    @Override
    public void onItemRemoved(CartItem item) {
        cartItems.remove(item);
        
        if (cartItems.isEmpty()) {
            showEmptyCartView();
        } else {
            adapter.notifyDataSetChanged();
            double totalPrice = adapter.calculateTotalPrice();
            totalPriceTextView.setText(currencyFormatter.format(totalPrice));
        }

        updateCartInFirestore();
        
        Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show();
    }

    private void updateCartInFirestore() {
        String userId = currentUser.getUid();
        
        Map<String, Object> cartData = new HashMap<>();
        cartData.put("userId", userId);
        cartData.put("items", cartItems);

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

        Toast.makeText(this, "Proceeding to checkout...", Toast.LENGTH_SHORT).show();
    }
}
