package com.example.mealrecmmenderandroid.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Recipe implements Serializable {

    private String recipeId;
    private String recipeName;
    private String description;
    private String instructions;
    private String imageUrl;
    private String providerId;
    private String providerName;
    private String category;
    private String difficulty;
    private String cuisine;

    private int calories;
    private int preparationTime;
    private int cookingTime;
    private int servings;

    private double healthScore;
    private double protein;
    private double carbs;
    private double fat;
    private double fiber;

    // Rating fields
    private double averageRating;
    private int totalRatings;
    private Map<String, Double> userRatings;

    private Map<String, String> ingredients;
    private Map<String, IngredientDetail> ingredientDetails;

    private boolean approved;
    private long createdAt;

    // Empty constructor required for Firebase
    public Recipe() {
        this.ingredients = new HashMap<>();
        this.ingredientDetails = new HashMap<>();
        this.userRatings = new HashMap<>();
        this.averageRating = 0.0;
        this.totalRatings = 0;
    }

    // Ingredient Detail inner class
    public static class IngredientDetail implements Serializable {
        private String name;
        private String quantity;
        private String unit;

        public IngredientDetail() {}

        public IngredientDetail(String name, String quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    // Helper method to convert list to map
    @Exclude
    public void setIngredientsFromList(List<String> ingredientsList) {
        this.ingredients = new HashMap<>();
        for (int i = 0; i < ingredientsList.size(); i++) {
            this.ingredients.put(String.valueOf(i), ingredientsList.get(i));
        }
    }

    // Helper method to add a rating
    @Exclude
    public void addRating(String userId, double rating) {
        if (userRatings == null) {
            userRatings = new HashMap<>();
        }
        userRatings.put(userId, rating);
        calculateAverageRating();
    }

    // Calculate average rating from all user ratings
    @Exclude
    private void calculateAverageRating() {
        if (userRatings == null || userRatings.isEmpty()) {
            averageRating = 0.0;
            totalRatings = 0;
            return;
        }

        double sum = 0;
        for (Double rating : userRatings.values()) {
            sum += rating;
        }
        totalRatings = userRatings.size();
        averageRating = sum / totalRatings;
    }

    // All Getters and Setters
    public String getRecipeId() { return recipeId; }
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }

    public String getRecipeName() { return recipeName; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }

    public int getPreparationTime() { return preparationTime; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }

    public int getCookingTime() { return cookingTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }

    public double getHealthScore() { return healthScore; }
    public void setHealthScore(double healthScore) { this.healthScore = healthScore; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public double getFiber() { return fiber; }
    public void setFiber(double fiber) { this.fiber = fiber; }

    // Rating getters and setters
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getTotalRatings() { return totalRatings; }
    public void setTotalRatings(int totalRatings) { this.totalRatings = totalRatings; }

    public Map<String, Double> getUserRatings() { return userRatings; }
    public void setUserRatings(Map<String, Double> userRatings) { this.userRatings = userRatings; }

    public Map<String, String> getIngredients() { return ingredients; }
    public void setIngredients(Map<String, String> ingredients) { this.ingredients = ingredients; }

    public Map<String, IngredientDetail> getIngredientDetails() { return ingredientDetails; }
    public void setIngredientDetails(Map<String, IngredientDetail> ingredientDetails) {
        this.ingredientDetails = ingredientDetails;
    }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}