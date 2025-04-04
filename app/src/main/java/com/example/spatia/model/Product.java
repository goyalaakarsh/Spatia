package com.example.spatia.model;

import com.google.gson.annotations.SerializedName;

public class Product {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("category")
    private String category;

    @SerializedName("imageUrl")
    private String imageUrl;
    
    @SerializedName("modelUrl")
    private String modelUrl; // URL for 3D model (GLB format)

    // No-args constructor (required for Gson)
    public Product() {
    }

    // Full constructor
    public Product(int id, String name, String description, double price, String category, String imageUrl, String modelUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.modelUrl = modelUrl;
    }

    // Simplified constructor
    public Product(long id, String name, double price) {
        this.id = (int) id;
        this.name = name;
        this.price = price;
        this.description = "";
        this.category = "";
        this.imageUrl = "";
        this.modelUrl = "";
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getModelUrl() {
        return modelUrl;
    }
    
    public void setModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }
    
    /**
     * Checks if the product has a valid 3D model available for AR view
     * @return true if a model URL is available, false otherwise
     */
    public boolean hasArModel() {
        return modelUrl != null && !modelUrl.isEmpty();
    }
}