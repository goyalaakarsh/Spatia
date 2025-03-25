package com.example.spatia.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model class for user data
 */
public class User {
    @SerializedName("id")
    private long id;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("firstName")
    private String firstName;
    
    @SerializedName("lastName")
    private String lastName;
    
    /**
     * Full constructor
     */
    public User(long id, String username, String email, String firstName, String lastName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    /**
     * Constructor with fewer parameters
     */
    public User(long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = "";
        this.lastName = "";
    }
    
    // Getters and setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    /**
     * Get the full name (first + last)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}