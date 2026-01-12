package com.example.mealrecmmenderandroid.models;

public class User {
    private String userId;
    private String username;        // NEW - for display name
    private String fullName;
    private String email;
    private String phone;
    private String userType;        // "consumer" or "provider"
    private String accountType;     // NEW - alternative to userType for consistency
    private int totalRecipesProvided;
    private double averageRating;
    private int ratingCount;
    private double totalRating;     // NEW - for calculating average
    private long registrationDate;  // NEW - timestamp

    public User() {
        // Default constructor required for Firebase
    }

    public User(String userId, String fullName, String email, String phone, String userType) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = fullName;   // Initialize username with fullName
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.accountType = userType;
        this.totalRecipesProvided = 0;
        this.averageRating = 0.0;
        this.ratingCount = 0;
        this.totalRating = 0.0;
        this.registrationDate = System.currentTimeMillis();
    }

    // NEW - Constructor with username
    public User(String userId, String username, String email, String userType) {
        this.userId = userId;
        this.username = username;
        this.fullName = username;
        this.email = email;
        this.userType = userType;
        this.accountType = userType;
        this.totalRecipesProvided = 0;
        this.averageRating = 0.0;
        this.ratingCount = 0;
        this.totalRating = 0.0;
        this.registrationDate = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        // Fallback to fullName if username is null
        if (username == null || username.isEmpty()) {
            return fullName;
        }
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getAccountType() {
        // Fallback to userType if accountType is null
        if (accountType == null || accountType.isEmpty()) {
            return userType;
        }
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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

    public double getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(double totalRating) {
        this.totalRating = totalRating;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }
}