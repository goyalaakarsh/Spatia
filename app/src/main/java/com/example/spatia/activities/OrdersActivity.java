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
import com.example.spatia.adapters.OrderAdapter;
import com.example.spatia.model.Order;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersActivity extends BaseActivity implements OrderAdapter.OrderClickListener {

    private static final String TAG = "OrdersActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    // UI Components
    private RecyclerView ordersRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyOrdersText;
    private OrderAdapter adapter;

    // Data
    private List<Order> orders;
    private String indexCreationUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.orders);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        setupNavigation();

        if (currentUser == null) {
            Toast.makeText(this, "Please login to view your orders", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        // Initialize UI components
        initViews();
        
        // Initialize data
        orders = new ArrayList<>();
        
        // Setup RecyclerView
        adapter = new OrderAdapter(this, orders, this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);
        
        // Load orders
        loadOrders();
    }

    private void initViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        progressBar = findViewById(R.id.ordersProgressBar);
        emptyOrdersText = findViewById(R.id.emptyOrdersText);
    }

    private void loadOrders() {
        showProgress(true);
        showEmptyState(false);
        
        try {
            // First try with just userId filter without ordering
            db.collection("orders")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showProgress(false);
                    processOrdersResult(queryDocumentSnapshots.getDocuments());
                    
                    // After successfully loading, log that we should create an index for better performance
                    Log.i(TAG, "Orders loaded successfully without ordering. Consider creating an index for better performance.");
                    
                    // Only show index creation message if we have the URL and there are actually orders
                    if (indexCreationUrl != null && !orders.isEmpty()) {
                        Toast.makeText(OrdersActivity.this, 
                            "For better sorting, create the required database index.", 
                            Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Log.e(TAG, "Error getting orders", e);
                    
                    // Extract index creation URL if available
                    if (e.getMessage() != null && e.getMessage().contains("https://console.firebase.google.com")) {
                        extractIndexUrl(e.getMessage());
                    }
                    
                    Toast.makeText(OrdersActivity.this, 
                            "Failed to load orders. Please try again later.", 
                            Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                });
        } catch (Exception e) {
            showProgress(false);
            Log.e(TAG, "Unexpected error in loadOrders", e);
            Toast.makeText(OrdersActivity.this, 
                    "Error loading orders: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            showEmptyState(true);
        }
    }
    
    private void processOrdersResult(List<DocumentSnapshot> documents) {
        orders.clear();
        for (DocumentSnapshot document : documents) {
            try {
                String orderId = document.getId();
                String userId = document.getString("userId");
                
                // Using null-safe retrieval with defaults
                Double subtotalObj = document.getDouble("subtotal");
                Double deliveryFeeObj = document.getDouble("deliveryFee");
                Double totalAmountObj = document.getDouble("totalAmount");
                
                double subtotal = subtotalObj != null ? subtotalObj : 0.0;
                double deliveryFee = deliveryFeeObj != null ? deliveryFeeObj : 0.0;
                double totalAmount = totalAmountObj != null ? totalAmountObj : 0.0;
                
                String paymentMethod = document.getString("paymentMethod");
                String paymentStatus = document.getString("paymentStatus");
                String orderStatus = document.getString("orderStatus");
                Timestamp orderDate = document.getTimestamp("orderDate");
                
                Order order = new Order(orderId, userId, subtotal, deliveryFee, 
                        totalAmount, paymentMethod, paymentStatus, orderStatus, orderDate);
                
                // Get address if available
                Map<String, Object> address = (Map<String, Object>) document.get("address");
                if (address != null) {
                    order.setAddress(address);
                }
                
                orders.add(order);
                Log.d(TAG, "Added order: " + orderId);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing order: " + document.getId(), e);
            }
        }
        
        // Sort orders by date locally, since we can't rely on database ordering
        sortOrdersByDate();
        
        adapter.notifyDataSetChanged();
        
        if (orders.isEmpty()) {
            showEmptyState(true);
        } else {
            showEmptyState(false);
        }
    }
    
    private void sortOrdersByDate() {
        if (orders.isEmpty()) return;
        
        orders.sort((o1, o2) -> {
            // Handle null timestamps
            if (o1.getOrderDate() == null && o2.getOrderDate() == null) return 0;
            if (o1.getOrderDate() == null) return 1;  // Null dates go at the end
            if (o2.getOrderDate() == null) return -1;
            
            // Sort in descending order (newest first)
            return o2.getOrderDate().compareTo(o1.getOrderDate());
        });
    }

    // Extract index creation URL from error message
    private void extractIndexUrl(String errorMessage) {
        if (errorMessage == null) return;
        
        int startIndex = errorMessage.indexOf("https://console.firebase.google.com");
        if (startIndex != -1) {
            int endIndex = errorMessage.indexOf(" ", startIndex);
            if (endIndex == -1) endIndex = errorMessage.length();
            
            indexCreationUrl = errorMessage.substring(startIndex, endIndex);
            Log.i(TAG, "Index creation URL: " + indexCreationUrl);
            
            // Log statement that provides clear guidance for developers
            Log.i(TAG, "TO FIX ORDERING: Create a composite index in Firebase by opening this URL: " + 
                  indexCreationUrl);
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show) {
        emptyOrdersText.setVisibility(show ? View.VISIBLE : View.GONE);
        ordersRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onOrderClicked(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", order.getOrderId());
        startActivity(intent);
    }
    
    // Add helper method to display guidance for creating index
    private void showIndexHelp(String errorMessage) {
        if (errorMessage != null && errorMessage.contains("https://")) {
            int startIdx = errorMessage.indexOf("https://");
            if (startIdx != -1) {
                int endIdx = errorMessage.indexOf(" ", startIdx);
                if (endIdx == -1) endIdx = errorMessage.length();
                String indexUrl = errorMessage.substring(startIdx, endIdx);
                Log.i(TAG, "Create Firestore index at: " + indexUrl);
                
                // Here you could store the URL for action (e.g., open in browser)
                // or show a dialog with more detailed instructions
            }
        }
    }

    protected String getActivityTitle() {
        return "Your Orders";
    }
}
