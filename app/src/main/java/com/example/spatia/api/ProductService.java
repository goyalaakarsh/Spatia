package com.example.spatia.api;

import com.example.spatia.model.Product;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ProductService {
    @GET("products")
    Call<List<Product>> getAllProducts();
    
    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") int productId);
    
    @GET("products/category/{category}")
    Call<List<Product>> getProductsByCategory(@Path("category") String category);

    @GET("products/search")
    Call<List<Product>> searchProducts(@Query("q") String query);
}
