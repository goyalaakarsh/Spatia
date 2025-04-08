package com.example.spatia.activities;

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
import com.example.spatia.adapters.OrderItemAdapter;
import com.example.spatia.model.OrderItem;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailActivity";

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UI Components
    private RecyclerView orderItemsRecyclerView;
    private TextView orderIdText;
    private TextView orderDateText;
    private TextView orderStatusText;
    private TextView paymentMethodText;
    private TextView paymentStatusText;
    private TextView subtotalText;
    private TextView deliveryFeeText;
    private TextView totalAmountText;
    private TextView deliveryAddressText;
    private ProgressBar progressBar;

    // Data
    private String orderId;
    private List<OrderItem> orderItems;
    private NumberFormat currencyFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_detail);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get order ID from intent
        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading order details for: " + orderId);

        // Initialize UI components
        initViews();
        
        // Initialize data
        orderItems = new ArrayList<>();
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        
        // Setup RecyclerView
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Load order details
        loadOrderDetails();
    }

    private void initViews() {
        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView);
        orderIdText = findViewById(R.id.orderIdValue);
        orderDateText = findViewById(R.id.orderDateValue);
        orderStatusText = findViewById(R.id.orderStatusValue);
        paymentMethodText = findViewById(R.id.paymentMethodValue);
        paymentStatusText = findViewById(R.id.paymentStatusValue);
        subtotalText = findViewById(R.id.subtotalValue);
        deliveryFeeText = findViewById(R.id.deliveryFeeValue);
        totalAmountText = findViewById(R.id.totalAmountValue);
        deliveryAddressText = findViewById(R.id.deliveryAddressValue);
        progressBar = findViewById(R.id.orderDetailProgressBar);
    }

    private void loadOrderDetails() {
        showProgress(true);
        
        db.collection("orders").document(orderId)
                .get()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            displayOrderDetails(document);
                        } else {
                            Log.d(TAG, "No such document");
                            Toast.makeText(OrderDetailActivity.this, "Order not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                        Toast.makeText(OrderDetailActivity.this, 
                                "Error loading order details: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void displayOrderDetails(DocumentSnapshot document) {
        try {
            // Set order details
            String displayOrderId = document.getString("orderId");
            if (displayOrderId == null) displayOrderId = document.getId();
            orderIdText.setText("#" + displayOrderId.substring(0, Math.min(8, displayOrderId.length())));
            
            // Format and set order date
            Timestamp orderDate = document.getTimestamp("orderDate");
            if (orderDate != null) {
                Date date = orderDate.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                orderDateText.setText(sdf.format(date));
            }
            
            String orderStatus = document.getString("orderStatus");
            orderStatusText.setText(orderStatus != null ? orderStatus : "Processing");
            
            String paymentMethod = document.getString("paymentMethod");
            paymentMethodText.setText(paymentMethod != null ? paymentMethod : "Pay on Delivery");
            
            String paymentStatus = document.getString("paymentStatus");
            paymentStatusText.setText(paymentStatus != null ? paymentStatus : "Pending");
            
            Double subtotal = document.getDouble("subtotal");
            Double deliveryFee = document.getDouble("deliveryFee");
            Double totalAmount = document.getDouble("totalAmount");
            
            subtotalText.setText(currencyFormatter.format(subtotal != null ? subtotal : 0));
            deliveryFeeText.setText(currencyFormatter.format(deliveryFee != null ? deliveryFee : 0));
            totalAmountText.setText(currencyFormatter.format(totalAmount != null ? totalAmount : 0));
            
            // Format and set delivery address
            Map<String, Object> address = (Map<String, Object>) document.get("address");
            if (address != null) {
                StringBuilder formattedAddress = new StringBuilder();
                if (address.get("name") != null) formattedAddress.append(address.get("name")).append("\n");
                if (address.get("phone") != null) formattedAddress.append(address.get("phone")).append("\n");
                if (address.get("addressLine1") != null) formattedAddress.append(address.get("addressLine1")).append("\n");
                
                if (address.get("addressLine2") != null && !address.get("addressLine2").toString().isEmpty()) {
                    formattedAddress.append(address.get("addressLine2")).append("\n");
                }
                
                if (address.get("city") != null) formattedAddress.append(address.get("city")).append(", ");
                if (address.get("state") != null) formattedAddress.append(address.get("state")).append(" - ");
                if (address.get("pincode") != null) formattedAddress.append(address.get("pincode"));
                
                deliveryAddressText.setText(formattedAddress.toString());
            }
            
            // Set status color
            if (orderStatus != null) {
                int statusColor;
                switch (orderStatus) {
                    case "Delivered":
                        statusColor = getResources().getColor(android.R.color.holo_green_dark);
                        break;
                    case "Cancelled":
                        statusColor = getResources().getColor(android.R.color.holo_red_dark);
                        break;
                    case "Shipped":
                        statusColor = getResources().getColor(R.color.pri_yellow);
                        break;
                    default:
                        statusColor = getResources().getColor(R.color.primary_dark);
                        break;
                }
                orderStatusText.setTextColor(statusColor);
            }
            
            // Get order items
            List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");
            if (items != null && !items.isEmpty()) {
                orderItems.clear();
                for (Map<String, Object> item : items) {
                    try {
                        int productId = 0;
                        int quantity = 1;
                        
                        if (item.get("productId") instanceof Long) {
                            productId = ((Long) item.get("productId")).intValue();
                        }
                        
                        if (item.get("quantity") instanceof Long) {
                            quantity = ((Long) item.get("quantity")).intValue();
                        }
                        
                        String productName = (String) item.get("productName");
                        double productPrice = 0;
                        if (item.get("productPrice") instanceof Double) {
                            productPrice = (double) item.get("productPrice");
                        }
                        String productImage = (String) item.get("productImage");
                        
                        OrderItem orderItem = new OrderItem(productId, quantity);
                        orderItem.setProductName(productName != null ? productName : "Product");
                        orderItem.setProductPrice(productPrice);
                        orderItem.setProductImage(productImage);
                        
                        orderItems.add(orderItem);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing order item", e);
                    }
                }
                
                // Set adapter for recycler view
                OrderItemAdapter adapter = new OrderItemAdapter(this, orderItems);
                orderItemsRecyclerView.setAdapter(adapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying order details", e);
            Toast.makeText(this, "Error displaying order details", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
