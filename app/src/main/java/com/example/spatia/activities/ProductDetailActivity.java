package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.spatia.R;
import com.example.spatia.adapters.ProductAdapter;
//import com.example.spatia.ar.ArUtils;
import com.example.spatia.model.CartItem;
import com.example.spatia.model.Product;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends BaseActivity {

    private static final String TAG = "ProductDetailActivity";
    
    private ImageView productImage;
    private TextView productName, productPrice, productCount, tabContent, reviewCount;
    private RatingBar productRating;
    private Button addToCartButton;
    private MaterialButton viewInRoomButton;
    private RecyclerView similarProductsRecycler;
    private TextView tabDescription, tabMaterials;
    
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Product currentProduct;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_details);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        // Initialize views
        initViews();
        setupNavigation();
        
        // Get product data from intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("product_id")) {
            int productId = intent.getIntExtra("product_id", -1);
            if (productId != -1) {
                loadProductDetails(productId);
            } else {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Product information missing", Toast.LENGTH_SHORT).show();
            finish();
        }
        
        // Set up click listeners
        setupClickListeners();
    }
    
    private void initViews() {
        productImage = findViewById(R.id.product_image);
        productName = findViewById(R.id.product_name);
        productPrice = findViewById(R.id.product_price);
        productCount = findViewById(R.id.product_count);
        tabContent = findViewById(R.id.tab_content);
        reviewCount = findViewById(R.id.review_count);
        productRating = findViewById(R.id.product_rating);
        addToCartButton = findViewById(R.id.btn_add_to_cart);
        viewInRoomButton = findViewById(R.id.btn_view_in_room);
        similarProductsRecycler = findViewById(R.id.similar_products_recycler);
        tabDescription = findViewById(R.id.tab_description);
        tabMaterials = findViewById(R.id.tab_materials);
        
        // Set up similar products recycler view
        similarProductsRecycler.setLayoutManager(
            new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }
    
    private void setupClickListeners() {
        // Quantity controls
        findViewById(R.id.btn_decrease).setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                productCount.setText(String.valueOf(quantity));
            }
        });
        
        findViewById(R.id.btn_increase).setOnClickListener(v -> {
            quantity++;
            productCount.setText(String.valueOf(quantity));
        });
        
        // Tab selection
        tabDescription.setOnClickListener(v -> {
            tabDescription.setBackgroundResource(R.drawable.tab_selected_background);
            tabMaterials.setBackground(null);
            if (currentProduct != null) {
                tabContent.setText(currentProduct.getDescription());
            }
        });
        
        tabMaterials.setOnClickListener(v -> {
            tabMaterials.setBackgroundResource(R.drawable.tab_selected_background);
            tabDescription.setBackground(null);
            // For demo purposes - you would typically have materials info in your product model
            tabContent.setText("Materials information would be displayed here.");
        });
        
        // Add to cart button
        addToCartButton.setOnClickListener(v -> {
            if (currentProduct != null) {
                addToCart(currentProduct.getId(), quantity);
            }
        });
        
        // View in Room (AR) button
//        viewInRoomButton.setOnClickListener(v -> {
//            if (currentProduct != null && currentProduct.hasArModel()) {
//                // Launch AR view with the product's 3D model
//                ArUtils.launchArView(this, currentProduct.getModelUrl());
//            } else {
//                Toast.makeText(this, "3D model not available for this product", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
    
    private void loadProductDetails(int productId) {
        db.collection("products")
            .whereEqualTo("id", productId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    // Get the first matching product
                    QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                    currentProduct = document.toObject(Product.class);
                    displayProductDetails();
                    loadSimilarProducts(currentProduct.getCategory());
                } else {
                    Toast.makeText(this, "Error loading product details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
    }
    
    private void displayProductDetails() {
        // Display product information
        productName.setText(currentProduct.getName());
        productPrice.setText("₹" + currentProduct.getPrice());
        tabContent.setText(currentProduct.getDescription());
        
        // Load product image
        Glide.with(this)
            .load(currentProduct.getImageUrl())
            .centerCrop()
            .into(productImage);
            
        // Show/hide AR button based on model availability
//        viewInRoomButton.setVisibility(
//            currentProduct.hasArModel() && ArUtils.isArSupported(this) ?
//            View.VISIBLE : View.GONE);
            
        // For demo - set rating and review count
        productRating.setRating(4.5f);
        reviewCount.setText("(128 reviews)");
    }
    
    private void loadSimilarProducts(String category) {
        List<Product> similarProducts = new ArrayList<>();
        
        db.collection("products")
            .whereEqualTo("category", category)
            .limit(5)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Product product = document.toObject(Product.class);
                    // Don't include the current product in similar products
                    if (product.getId() != currentProduct.getId()) {
                        similarProducts.add(product);
                    }
                }
                
                // Set up adapter for similar products
                ProductAdapter adapter = new ProductAdapter(this, similarProducts);
                similarProductsRecycler.setAdapter(adapter);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading similar products", Toast.LENGTH_SHORT).show();
            });
    }
    
    private void addToCart(int productId, int quantity) {
        if (currentUser == null) {
            // If user is not logged in, redirect to login
            Toast.makeText(this, "Please log in to add items to cart", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            return;
        }
        
        String userId = currentUser.getUid();
        
        // First check if user already has a cart
        db.collection("carts").document(userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Cart exists, check if item already exists
                        List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");
                        boolean itemExists = false;
                        
                        if (items != null) {
                            for (Map<String, Object> item : items) {
                                int itemProductId = ((Long) item.get("productId")).intValue();
                                if (itemProductId == productId) {
                                    // Item exists in cart, update quantity
                                    itemExists = true;
                                    updateCartItemQuantity(userId, productId, quantity);
                                    break;
                                }
                            }
                        }
                        
                        if (!itemExists) {
                            // Item doesn't exist in cart, add new item
                            addNewItemToCart(userId, productId, quantity);
                        }
                    } else {
                        // Cart doesn't exist, create new cart with item
                        createNewCart(userId, productId, quantity);
                    }
                } else {
                    Log.w(TAG, "Error checking cart", task.getException());
                    Toast.makeText(ProductDetailActivity.this, 
                            "Failed to access cart: " + task.getException().getMessage(), 
                            Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void updateCartItemQuantity(String userId, int productId, int quantityToAdd) {
        db.collection("carts").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                List<Map<String, Object>> items = (List<Map<String, Object>>) documentSnapshot.get("items");
                List<Map<String, Object>> updatedItems = new ArrayList<>();
                
                for (Map<String, Object> item : items) {
                    int itemProductId = ((Long) item.get("productId")).intValue();
                    int itemQuantity = ((Long) item.get("quantity")).intValue();
                    
                    if (itemProductId == productId) {
                        // Update this item's quantity
                        Map<String, Object> updatedItem = new HashMap<>();
                        updatedItem.put("productId", productId);
                        updatedItem.put("quantity", itemQuantity + quantityToAdd);
                        updatedItems.add(updatedItem);
                    } else {
                        // Keep item as is
                        updatedItems.add(item);
                    }
                }
                
                // Update the cart with modified items
                db.collection("carts").document(userId)
                    .update("items", updatedItems)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ProductDetailActivity.this, 
                                quantity + " item(s) added to cart", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error updating cart", e);
                        Toast.makeText(ProductDetailActivity.this, 
                                "Failed to update cart: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error getting cart details", e);
                Toast.makeText(ProductDetailActivity.this, 
                        "Failed to access cart: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            });
    }
    
    private void addNewItemToCart(String userId, int productId, int quantity) {
        Map<String, Object> newItem = new HashMap<>();
        newItem.put("productId", productId);
        newItem.put("quantity", quantity);
        
        // Add the new item to the items array
        db.collection("carts").document(userId)
            .update("items", FieldValue.arrayUnion(newItem))
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(ProductDetailActivity.this, 
                        quantity + " item(s) added to cart", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error adding item to cart", e);
                Toast.makeText(ProductDetailActivity.this, 
                        "Failed to add to cart: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            });
    }
    
    private void createNewCart(String userId, int productId, int quantity) {
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", productId);
        cartItem.put("quantity", quantity);
        
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(cartItem);
        
        Map<String, Object> cart = new HashMap<>();
        cart.put("userId", userId);
        cart.put("items", items);
        
        // Create new cart document
        db.collection("carts").document(userId)
            .set(cart)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(ProductDetailActivity.this, 
                        quantity + " item(s) added to cart", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.w(TAG, "Error creating cart", e);
                Toast.makeText(ProductDetailActivity.this, 
                        "Failed to create cart: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            });
    }

    protected String getActivityTitle() {
        return "Details";
    }
}