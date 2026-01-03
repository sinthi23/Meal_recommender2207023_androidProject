package com.example.mealrecmmenderandroid.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User implements Serializable {
    private String userId;
    private String email;
    private String name;
    private String userType; // "consumer", "provider", "admin"
    private String phoneNumber;
    private String profileImageUrl;
    private String address;
    private long registrationDate;
    private long lastLoginDate;
    private boolean isActive;

    // Provider specific
    private double totalRating;
    private int ratingCount;
    private int totalRecipesProvided;
    private String businessName;
    private String businessLicense;

    // Consumer specific
    private String dietaryPreferences;
    private String allergies;
    private int targetCalories;

    public User() {
        this.registrationDate = System.currentTimeMillis();
        this.lastLoginDate = System.currentTimeMillis();
        this.isActive = true;
        this.totalRating = 0;
        this.ratingCount = 0;
        this.totalRecipesProvided = 0;
    }

    public User(String userId, String email, String name, String userType) {
        this();
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.userType = userType;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getUserType() { return userType; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getAddress() { return address; }
    public long getRegistrationDate() { return registrationDate; }
    public long getLastLoginDate() { return lastLoginDate; }
    public boolean isActive() { return isActive; }
    public double getTotalRating() { return totalRating; }
    public int getRatingCount() { return ratingCount; }
    public int getTotalRecipesProvided() { return totalRecipesProvided; }
    public String getBusinessName() { return businessName; }
    public String getBusinessLicense() { return businessLicense; }
    public String getDietaryPreferences() { return dietaryPreferences; }
    public String getAllergies() { return allergies; }
    public int getTargetCalories() { return targetCalories; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setUserType(String userType) { this.userType = userType; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public void setAddress(String address) { this.address = address; }
    public void setRegistrationDate(long registrationDate) { this.registrationDate = registrationDate; }
    public void setLastLoginDate(long lastLoginDate) { this.lastLoginDate = lastLoginDate; }
    public void setActive(boolean active) { isActive = active; }
    public void setTotalRating(double totalRating) { this.totalRating = totalRating; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public void setTotalRecipesProvided(int totalRecipesProvided) {
        this.totalRecipesProvided = totalRecipesProvided;
    }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public void setBusinessLicense(String businessLicense) { this.businessLicense = businessLicense; }
    public void setDietaryPreferences(String dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }
    public void setAllergies(String allergies) { this.allergies = allergies; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }

    @Exclude
    public double getAverageRating() {
        return ratingCount > 0 ? totalRating / ratingCount : 0;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("email", email);
        result.put("name", name);
        result.put("userType", userType);
        result.put("phoneNumber", phoneNumber);
        result.put("profileImageUrl", profileImageUrl);
        result.put("address", address);
        result.put("registrationDate", registrationDate);
        result.put("lastLoginDate", lastLoginDate);
        result.put("isActive", isActive);
        result.put("totalRating", totalRating);
        result.put("ratingCount", ratingCount);
        result.put("totalRecipesProvided", totalRecipesProvided);
        result.put("businessName", businessName);
        result.put("businessLicense", businessLicense);
        result.put("dietaryPreferences", dietaryPreferences);
        result.put("allergies", allergies);
        result.put("targetCalories", targetCalories);
        return result;
    }
}