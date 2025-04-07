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
    private String modelUrl; 
    
    @SerializedName("modelUrlGLB")
    private String modelUrlGLB;
    
    @SerializedName("modelUrlGLTF")
    private String modelUrlGLTF; 
    
    @SerializedName("modelUrlGLBBR")
    private String modelUrlGLBBR; 
    
    @SerializedName("material")
    private String material;
    
    @SerializedName("rating")
    private double rating;

    // No-args constructor (required for Gson)
    public Product() {
    }

    // Full constructor
    public Product(int id, String name, String description, double price, String category, String imageUrl, String modelUrl, String material, double rating, String modelUrlGLB, String modelUrlGLTF, String modelUrlGLBBR) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.modelUrl = modelUrl;
        this.material = material;
        this.rating = rating;
        this.modelUrlGLB = modelUrlGLB;
        this.modelUrlGLTF = modelUrlGLTF;
        this.modelUrlGLBBR = modelUrlGLBBR;
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
        this.material = "";
        this.rating = 0.0;
        this.modelUrlGLB = "";
        this.modelUrlGLTF = "";
        this.modelUrlGLBBR = "";
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
    
    public String getMaterial() {
        return material;
    }
    
    public void setMaterial(String material) {
        this.material = material;
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public String getModelUrlGLB() {
        return modelUrlGLB;
    }
    
    public void setModelUrlGLB(String modelUrlGLB) {
        this.modelUrlGLB = modelUrlGLB;
    }
    
    public String getModelUrlGLTF() {
        return modelUrlGLTF;
    }
    
    public void setModelUrlGLTF(String modelUrlGLTF) {
        this.modelUrlGLTF = modelUrlGLTF;
    }
    
    public String getModelUrlGLBBR() {
        return modelUrlGLBBR;
    }
    
    public void setModelUrlGLBBR(String modelUrlGLBBR) {
        this.modelUrlGLBBR = modelUrlGLBBR;
    }
    
    /**
     * Checks if the product has a valid 3D model available for AR view
     * @return true if a model URL is available, false otherwise
     */
    public boolean hasArModel() {
        return modelUrl != null && !modelUrl.isEmpty() || modelUrlGLB != null && !modelUrlGLB.isEmpty() || modelUrlGLTF != null && !modelUrlGLTF.isEmpty() || modelUrlGLBBR != null && !modelUrlGLBBR.isEmpty();
    }
    
    /**
     * Gets the best available 3D model URL for the current device
     * Prioritizes GLB format as it's most widely supported
     * @return The URL of the best available 3D model or null if none available
     */
    public String getBestModelUrl() {
        if (modelUrlGLB != null && !modelUrlGLB.isEmpty()) {
            return modelUrlGLB;
        } else if (modelUrlGLTF != null && !modelUrlGLTF.isEmpty()) {
            return modelUrlGLTF;
        } else if (modelUrlGLBBR != null && !modelUrlGLBBR.isEmpty()) {
            return modelUrlGLBBR;
        } else if (modelUrl != null && !modelUrl.isEmpty()) {
            return modelUrl;
        }
        return null;
    }
}
