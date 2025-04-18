package com.example.spatia.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
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
import com.google.android.gms.tasks.OnCompleteListener;
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
     * Query Firestore for products whose name exactly matches the query.
     */
    private void performSearch(String query) {
        // UI feedback
        progressBar.setVisibility(ProgressBar.VISIBLE);
        noResultsText.setVisibility(TextView.GONE);

        // Firestore query: exact match on "name" field
        db.collection("products")
                .whereEqualTo("name", query)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        progressBar.setVisibility(ProgressBar.GONE);
                        if (task.isSuccessful()) {
                            productList.clear();
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                Product p = doc.toObject(Product.class);
                                // Only add if it has an image
                                if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
                                    productList.add(p);
                                }
                            }
                            adapter.notifyDataSetChanged();

                            // Show "no results" if list is empty
                            if (productList.isEmpty()) {
                                noResultsText.setVisibility(TextView.VISIBLE);
                            }
                        } else {
                            Toast.makeText(SearchActivity.this,
                                    "Search failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
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
