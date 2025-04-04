package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.spatia.model.Product;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    
    private ImageView productImage;
    private TextView productName, productPrice, productCount, tabContent, reviewCount;
    private RatingBar productRating;
    private Button addToCartButton;
    private MaterialButton viewInRoomButton;
    private RecyclerView similarProductsRecycler;
    private TextView tabDescription, tabMaterials;
    
    private FirebaseFirestore db;
    private Product currentProduct;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_details);
        
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        
        // Initialize views
        initViews();
        
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
            // Implement add to cart functionality
            Toast.makeText(this, quantity + " item(s) added to cart", Toast.LENGTH_SHORT).show();
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
}