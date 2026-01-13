package com.example.mealrecmmenderandroid.models;

import java.io.Serializable;

public class CookHistory implements Serializable {
    private String historyId;
    private String userId;
    private String recipeId;
    private String recipeName;
    private int calories;
    private long timestamp;
    private float rating;  // Make sure this exists
    private String notes;

    public CookHistory() {
        // Required empty constructor for Firebase
    }

    public CookHistory(String historyId, String userId, String recipeId, String recipeName,
                       int calories, long timestamp, float rating, String notes) {
        this.historyId = historyId;
        this.userId = userId;
        this.recipeId = recipeId;
        this.recipeName = recipeName;
        this.calories = calories;
        this.timestamp = timestamp;
        this.rating = rating;
        this.notes = notes;
    }

    // Getters and Setters
    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}