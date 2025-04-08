package com.example.spatia.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

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

    @SerializedName("address")
    private Map<String, Object> address; // Changed from String to Map<String, Object>

    @SerializedName("phone")
    private String phone;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    public User() {
    }

    public User(String id, String name, String email, Map<String, Object> address, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.profileImageUrl = "";
    }
    
    public User(String id, String email) {
        this.id = id;
        this.email = email;
        this.name = "";
        this.phone = "";
        this.address = null;
        this.profileImageUrl = "";
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

    public Map<String, Object> getAddress() {
        return address;
    }

    public void setAddress(Map<String, Object> address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    // Helper method to get formatted address
    public String getFormattedAddress() {
        if (address == null || address.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        
        // Safely append address components if they exist
        if (address.containsKey("addressLine1")) {
            sb.append(address.get("addressLine1"));
        }
        
        if (address.containsKey("addressLine2") && address.get("addressLine2") != null 
                && !address.get("addressLine2").toString().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.get("addressLine2"));
        }
        
        if (address.containsKey("city")) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.get("city"));
        }
        
        if (address.containsKey("state")) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.get("state"));
        }
        
        if (address.containsKey("pincode")) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(address.get("pincode"));
        }
        
        return sb.toString();
    }
}