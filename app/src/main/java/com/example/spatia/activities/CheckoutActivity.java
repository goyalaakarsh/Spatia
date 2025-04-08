package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spatia.R;
import com.example.spatia.adapters.CheckoutItemAdapter;
import com.example.spatia.model.CartItem;
import com.example.spatia.model.Order;
import com.example.spatia.model.OrderItem;
import com.example.spatia.model.Product;
import com.example.spatia.model.UserAddress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CheckoutActivity extends BaseActivity {

    private static final String TAG = "CheckoutActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    // UI Components
    private RecyclerView orderItemsRecyclerView;
    private TextView subtotalTextView;
    private TextView deliveryFeeTextView;
    private TextView totalAmountTextView;
    private ProgressBar progressBar;
    private Button placeOrderButton;
    private TextView addressTextView;
    private Button editAddressButton;
    private CardView addressCard;
    private CardView editAddressCard;
    private TextInputEditText nameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText addressLine1Input;
    private TextInputEditText addressLine2Input;
    private TextInputEditText cityInput;
    private TextInputEditText stateInput;
    private TextInputEditText pincodeInput;
    private Button saveAddressButton;
    private Button cancelEditButton;
    private RadioGroup paymentMethodGroup;

    // Data
    private List<CartItem> cartItems;
    private Map<Integer, Product> productMap;
    private double subtotal = 0;
    private double deliveryFee = 50;
    private double totalAmount = 0;
    private UserAddress userAddress;
    private NumberFormat currencyFormatter;
    private String selectedPaymentMethod = "Pay on Delivery";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        setupNavigation();

        if (currentUser == null) {
            Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        initViews();

        cartItems = new ArrayList<>();
        productMap = new HashMap<>();
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        setupUI();

        loadCartItems();
        loadUserAddress();
    }

    private void initViews() {
        orderItemsRecyclerView = findViewById(R.id.checkoutItemsRecyclerView);
        subtotalTextView = findViewById(R.id.subtotalValue);
        deliveryFeeTextView = findViewById(R.id.deliveryFeeValue);
        totalAmountTextView = findViewById(R.id.totalAmountValue);
        progressBar = findViewById(R.id.checkoutProgressBar);
        placeOrderButton = findViewById(R.id.placeOrderButton);
        addressTextView = findViewById(R.id.addressText);
        editAddressButton = findViewById(R.id.editAddressButton);
        addressCard = findViewById(R.id.addressCard);
        editAddressCard = findViewById(R.id.editAddressCard);
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        addressLine1Input = findViewById(R.id.addressLine1Input);
        addressLine2Input = findViewById(R.id.addressLine2Input);
        cityInput = findViewById(R.id.cityInput);
        stateInput = findViewById(R.id.stateInput);
        pincodeInput = findViewById(R.id.pincodeInput);
        saveAddressButton = findViewById(R.id.saveAddressButton);
        cancelEditButton = findViewById(R.id.cancelEditButton);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
    }

    private void setupUI() {
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deliveryFeeTextView.setText(currencyFormatter.format(deliveryFee));
        editAddressButton.setOnClickListener(v -> showEditAddressForm());
        saveAddressButton.setOnClickListener(v -> saveUserAddress());
        cancelEditButton.setOnClickListener(v -> hideEditAddressForm());
        placeOrderButton.setOnClickListener(v -> placeOrder());
        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = findViewById(checkedId);
            if (selectedRadioButton != null) {
                selectedPaymentMethod = selectedRadioButton.getText().toString();
            }
        });
    }

    private void loadCartItems() {
        showProgress(true);
        
        db.collection("carts").document(currentUser.getUid())
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
                                showEmptyState();
                            }
                        } else {
                            showEmptyState();
                        }
                    } else {
                        Log.w(TAG, "Error getting cart items", task.getException());
                        Toast.makeText(CheckoutActivity.this, 
                                "Failed to load cart: " + task.getException().getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                });
    }

    private void loadProductDetails() {
        if (cartItems.isEmpty()) {
            showEmptyState();
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
                            updateUI();
                        } else {
                            showEmptyState();
                        }
                    } else {
                        Log.w(TAG, "Error getting products", task.getException());
                        Toast.makeText(CheckoutActivity.this,
                                "Failed to load products: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                });
    }

    private void loadUserAddress() {
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> userData = document.getData();
                            if (userData != null && userData.containsKey("address")) {
                                Object addressObj = userData.get("address");

                                if (addressObj instanceof Map) {
                                    Map<String, Object> addressData = (Map<String, Object>) addressObj;
                                    userAddress = new UserAddress(
                                            (String) addressData.get("name"),
                                            (String) addressData.get("phone"),
                                            (String) addressData.get("addressLine1"),
                                            (String) addressData.get("addressLine2"),
                                            (String) addressData.get("city"),
                                            (String) addressData.get("state"),
                                            (String) addressData.get("pincode")
                                    );
                                    displayUserAddress();
                                } else if (addressObj instanceof String) {
                                    Log.w(TAG, "Address is stored as a String instead of a Map");
                                    showAddressUnavailable();
                                } else {
                                    Log.w(TAG, "Unknown address format: " + addressObj.getClass().getName());
                                    showAddressUnavailable();
                                }
                            } else {
                                showAddressUnavailable();
                            }
                        } else {
                            showAddressUnavailable();
                        }
                    } else {
                        Log.w(TAG, "Error getting user data", task.getException());
                        showAddressUnavailable();
                    }
                });
    }

    private void displayUserAddress() {
        if (userAddress == null) {
            showAddressUnavailable();
            return;
        }

        String formattedAddress = userAddress.getName() + "\n" +
                userAddress.getPhone() + "\n" +
                userAddress.getAddressLine1();
        
        if (!TextUtils.isEmpty(userAddress.getAddressLine2())) {
            formattedAddress += "\n" + userAddress.getAddressLine2();
        }
        
        formattedAddress += "\n" + userAddress.getCity() + ", " + 
                userAddress.getState() + " - " + userAddress.getPincode();
        
        addressTextView.setText(formattedAddress);
        addressCard.setVisibility(View.VISIBLE);
    }

    private void showAddressUnavailable() {
        addressTextView.setText("No delivery address available. Please add one.");
        editAddressButton.setText("Add Address");
        addressCard.setVisibility(View.VISIBLE);
    }

    private void showEditAddressForm() {
        if (userAddress != null) {
            nameInput.setText(userAddress.getName());
            phoneInput.setText(userAddress.getPhone());
            addressLine1Input.setText(userAddress.getAddressLine1());
            addressLine2Input.setText(userAddress.getAddressLine2());
            cityInput.setText(userAddress.getCity());
            stateInput.setText(userAddress.getState());
            pincodeInput.setText(userAddress.getPincode());
        }
        
        addressCard.setVisibility(View.GONE);
        editAddressCard.setVisibility(View.VISIBLE);
    }

    private void hideEditAddressForm() {
        addressCard.setVisibility(View.VISIBLE);
        editAddressCard.setVisibility(View.GONE);
    }

    private void saveUserAddress() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String addressLine1 = addressLine1Input.getText().toString().trim();
        String addressLine2 = addressLine2Input.getText().toString().trim();
        String city = cityInput.getText().toString().trim();
        String state = stateInput.getText().toString().trim();
        String pincode = pincodeInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(addressLine1) || 
                TextUtils.isEmpty(city) || TextUtils.isEmpty(state) || TextUtils.isEmpty(pincode)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        userAddress = new UserAddress(name, phone, addressLine1, addressLine2, city, state, pincode);
        
        Map<String, Object> addressData = new HashMap<>();
        addressData.put("name", name);
        addressData.put("phone", phone);
        addressData.put("addressLine1", addressLine1);
        addressData.put("addressLine2", addressLine2);
        addressData.put("city", city);
        addressData.put("state", state);
        addressData.put("pincode", pincode);
        
        db.collection("users").document(currentUser.getUid())
                .update("address", addressData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CheckoutActivity.this, "Address saved successfully", Toast.LENGTH_SHORT).show();
                    hideEditAddressForm();
                    displayUserAddress();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CheckoutActivity.this, "Failed to save address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        CheckoutItemAdapter adapter = new CheckoutItemAdapter(this, cartItems, productMap);
        orderItemsRecyclerView.setAdapter(adapter);

        subtotal = calculateSubtotal();
        subtotalTextView.setText(currencyFormatter.format(subtotal));

        totalAmount = subtotal + deliveryFee;
        totalAmountTextView.setText(currencyFormatter.format(totalAmount));
    }

    private double calculateSubtotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                total += product.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    private void placeOrder() {
        if (cartItems.isEmpty() || productMap.isEmpty()) {
            Toast.makeText(this, "Cannot place an empty order", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (userAddress == null) {
            Toast.makeText(this, "Please add a delivery address", Toast.LENGTH_SHORT).show();
            showEditAddressForm();
            return;
        }
        
        showProgress(true);
        
        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = productMap.get(cartItem.getProductId());
            if (product != null) {
                OrderItem orderItem = new OrderItem(
                        cartItem.getProductId(),
                        cartItem.getQuantity()
                );
                orderItems.add(orderItem);
            }
        }

        String orderId = UUID.randomUUID().toString();

        Map<String, Object> order = new HashMap<>();
        order.put("orderId", orderId);
        order.put("userId", currentUser.getUid());
        order.put("items", convertOrderItemsToMap(orderItems));
        order.put("subtotal", subtotal);
        order.put("deliveryFee", deliveryFee);
        order.put("totalAmount", totalAmount);
        order.put("paymentMethod", selectedPaymentMethod);
        order.put("paymentStatus", "Pending");
        order.put("orderStatus", "Processing");
        order.put("orderDate", new Timestamp(new Date()));
        order.put("address", userAddress.toMap());

        db.collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    clearCart();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Log.e(TAG, "Error creating order", e);
                    Toast.makeText(CheckoutActivity.this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private List<Map<String, Object>> convertOrderItemsToMap(List<OrderItem> orderItems) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (OrderItem item : orderItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProductId());
            itemMap.put("quantity", item.getQuantity());
            Product product = productMap.get(item.getProductId());
            if (product != null) {
                itemMap.put("productName", product.getName());
                itemMap.put("productPrice", product.getPrice());
                itemMap.put("productImage", product.getImageUrl());
            }
            items.add(itemMap);
        }
        return items;
    }

    private void clearCart() {
        db.collection("carts").document(currentUser.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    showOrderSuccessAndRedirect();
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Log.e(TAG, "Error clearing cart", e);
                    showOrderSuccessAndRedirect();
                });
    }

    private void showOrderSuccessAndRedirect() {
        Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, OrdersActivity.class);
        startActivity(intent);
        finish();
    }

    private void showEmptyState() {
        showProgress(false);
        Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        placeOrderButton.setEnabled(!show);
    }

    protected String getActivityTitle() {
        return "Checkout";
    }
}
