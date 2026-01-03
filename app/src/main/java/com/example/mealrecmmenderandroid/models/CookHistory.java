package com.example.mealrecmmenderandroid.models;

public class CookHistory {
    private String historyId;
    private String userId;
    private String recipeId;
    private String recipeName;
    private int calories;
    private long cookedDate;
    private float userRating;
    private String notes;

    // Required empty constructor for Firebase
    public CookHistory() {
    }

    public CookHistory(String historyId, String userId, String recipeId,
                       String recipeName, int calories, long cookedDate,
                       float userRating, String notes) {
        this.historyId = historyId;
        this.userId = userId;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.calories = calories;
        this.cookedDate = cookedDate;
        this.userRating = userRating;
        this.notes = notes;
    }

    // Getters
    public String getHistoryId() {
        return historyId;
    }

    public String getUserId() {
        return userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public int getCalories() {
        return calories;
    }

    public long getCookedDate() {
        return cookedDate;
    }

    public float getUserRating() {
        return userRating;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public void setCookedDate(long cookedDate) {
        this.cookedDate = cookedDate;
    }

    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}