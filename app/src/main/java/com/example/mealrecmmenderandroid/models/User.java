package com.example.mealrecmmenderandroid.models;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String userType; // "consumer" or "provider"
    private int totalRecipesProvided;
    private double averageRating;
    private int ratingCount;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String fullName, String email, String phone, String userType) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.totalRecipesProvided = 0;
        this.averageRating = 0.0;
        this.ratingCount = 0;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public int getTotalRecipesProvided() {
        return totalRecipesProvided;
    }

    public void setTotalRecipesProvided(int totalRecipesProvided) {
        this.totalRecipesProvided = totalRecipesProvided;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }
}