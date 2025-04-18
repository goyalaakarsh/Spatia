package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spatia.R;
import com.example.spatia.adapters.ProductAdapter;
import com.example.spatia.model.Product;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {
    private EditText searchBar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView noResultsText;

    private ProductAdapter adapter;
    private List<Product> productList;
    private FirebaseFirestore db;

    // Debounce handler and runnable
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private static final long DEBOUNCE_DELAY_MS = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Firestore instance
        db = FirebaseFirestore.getInstance();

        // Optional: show back arrow in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupNavigation();


        // UI references
        searchBar      = findViewById(R.id.searchBar);
        recyclerView   = findViewById(R.id.searchRecyclerView);
        progressBar    = findViewById(R.id.searchProgressBar);
        noResultsText  = findViewById(R.id.noResultsText);

        // RecyclerView + Adapter setup
        productList = new ArrayList<>();
        adapter     = new ProductAdapter(this, productList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Add TextWatcher for debounce search
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String query = s.toString().trim();
                // Remove any pending callbacks
                if (debounceRunnable != null) {
                    debounceHandler.removeCallbacks(debounceRunnable);
                }
                debounceRunnable = () -> runOnUiThread(() -> {
                    if (!query.isEmpty()) {
                        performSearch(query);
                    } else {
                        productList.clear();
                        adapter.notifyDataSetChanged();
                        noResultsText.setVisibility(TextView.GONE);
                    }
                });
                debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Listen for IME "Search" action on keyboard
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String query = searchBar.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });
    }

    /**
     * Query Firestore for products whose name matches the query.
     */
    private void performSearch(String query) {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        noResultsText.setVisibility(TextView.GONE);

        // Convert query to lowercase for case-insensitive search
        String searchQuery = query.toLowerCase();

        db.collection("products")
            .get()
            .addOnCompleteListener(task -> {
                progressBar.setVisibility(ProgressBar.GONE);
                if (task.isSuccessful()) {
                    productList.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Product product = document.toObject(Product.class);
                        // Check if product name contains search query (case insensitive)
                        if (product.getName().toLowerCase().contains(searchQuery) && 
                            product.getImageUrl() != null && 
                            !product.getImageUrl().isEmpty()) {
                            productList.add(product);
                            Log.d("SearchActivity", "Product matched: " + product.getName());
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    // Update UI based on results
                    noResultsText.setVisibility(productList.isEmpty() ? TextView.VISIBLE : TextView.GONE);
                    Log.d("SearchActivity", "Found " + productList.size() + " products for query: " + query);
                    
                } else {
                    Log.e("SearchActivity", "Search failed", task.getException());
                    Toast.makeText(SearchActivity.this,
                            "Search failed: " + task.getException().getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void debugCheckProducts() {
        db.collection("products")
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("SearchActivity", "Total products in database: " + task.getResult().size());
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        Log.d("SearchActivity", "Product: " + doc.getData());
                    }
                } else {
                    Log.e("SearchActivity", "Error getting products", task.getException());
                }
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle toolbar back arrow
        finish();
        return true;
    }

    @Override
    protected String getActivityTitle() {
        return "Search";
    }
}
