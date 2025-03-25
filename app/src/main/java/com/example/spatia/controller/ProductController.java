package com.example.spatia.controller;

import android.content.Context;
import android.util.Log;

import com.example.spatia.api.ProductService;
import com.example.spatia.model.Product;
import com.example.spatia.repository.ProductRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductController {
    private static final String TAG = "ProductController";
    private final ProductService productService;
    private final ProductRepository productRepository;
    
    public ProductController(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.example.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
                
        productService = retrofit.create(ProductService.class);
        
        productRepository = new ProductRepository(context);
    }
    
    public void getAllProducts(final ProductCallback<List<Product>> callback) {
        productService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    // Save to local storage
                    productRepository.saveAllProducts(products);
                    callback.onSuccess(products);
                } else {
                    callback.onError("Error: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch products", t);
                callback.onError("Network error: " + t.getMessage());
                
                // Try to get products from local storage
                List<Product> cachedProducts = productRepository.getAllProducts();
                if (!cachedProducts.isEmpty()) {
                    callback.onSuccess(cachedProducts);
                }
            }
        });
    }

    public void getProductById(int productId, final ProductCallback<Product> callback) {
        productService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    productRepository.saveProduct(product);
                    callback.onSuccess(product);
                } else {
                    callback.onError("Error: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Log.e(TAG, "Failed to fetch product with ID: " + productId, t);
                callback.onError("Network error: " + t.getMessage());
                
                // Try to get from local storage
                Product cachedProduct = productRepository.getProductById(productId);
                if (cachedProduct != null) {
                    callback.onSuccess(cachedProduct);
                }
            }
        });
    }
    
    public interface ProductCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }
}
