package com.example.spatia.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model class for user data
 */
public class User {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("email")
    private String email;
    
    /**
     * Full constructor
     */
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}