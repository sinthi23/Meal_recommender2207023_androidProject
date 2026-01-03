package com.example.mealrecmmenderandroid.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class Recipe implements Serializable {
    private String recipeId;
    private String recipeName;
    private String description;
    private String providerId;
    private String providerName;
    private Map<String, String> ingredients; // Using Map for Firebase compatibility
    private Map<String, IngredientDetail> ingredientDetails;
    private String instructions;
    private int calories;
    private double healthScore;
    private int preparationTime;
    private int cookingTime;
    private int servings;
    private String category;
    private String cuisine;
    private String difficulty;
    private String imageUrl;
    private long createdDate;
    private long updatedDate;
    private double averageRating;
    private int ratingCount;
    private int viewCount;
    private int cookCount;
    private boolean isApproved;
    private boolean isFeatured;

    // Nutrition
    private double protein;
    private double carbs;
    private double fat;
    private double fiber;
    private double sugar;
    private double sodium;

    // Tags
    private Map<String, String> tags;

    public Recipe() {
        this.ingredients = new HashMap<>();
        this.ingredientDetails = new HashMap<>();
        this.tags = new HashMap<>();
        this.createdDate = System.currentTimeMillis();
        this.updatedDate = System.currentTimeMillis();
        this.averageRating = 0;
        this.ratingCount = 0;
        this.viewCount = 0;
        this.cookCount = 0;
        this.isApproved = false;
        this.isFeatured = false;
    }

    // Getters
    public String getRecipeId() { return recipeId; }
    public String getRecipeName() { return recipeName; }
    public String getDescription() { return description; }
    public String getProviderId() { return providerId; }
    public String getProviderName() { return providerName; }
    public Map<String, String> getIngredients() { return ingredients; }
    public Map<String, IngredientDetail> getIngredientDetails() { return ingredientDetails; }
    public String getInstructions() { return instructions; }
    public int getCalories() { return calories; }
    public double getHealthScore() { return healthScore; }
    public int getPreparationTime() { return preparationTime; }
    public int getCookingTime() { return cookingTime; }
    public int getServings() { return servings; }
    public String getCategory() { return category; }
    public String getCuisine() { return cuisine; }
    public String getDifficulty() { return difficulty; }
    public String getImageUrl() { return imageUrl; }
    public long getCreatedDate() { return createdDate; }
    public long getUpdatedDate() { return updatedDate; }
    public double getAverageRating() { return averageRating; }
    public int getRatingCount() { return ratingCount; }
    public int getViewCount() { return viewCount; }
    public int getCookCount() { return cookCount; }
    public boolean isApproved() { return isApproved; }
    public boolean isFeatured() { return isFeatured; }
    public double getProtein() { return protein; }
    public double getCarbs() { return carbs; }
    public double getFat() { return fat; }
    public double getFiber() { return fiber; }
    public double getSugar() { return sugar; }
    public double getSodium() { return sodium; }
    public Map<String, String> getTags() { return tags; }

    // Setters
    public void setRecipeId(String recipeId) { this.recipeId = recipeId; }
    public void setRecipeName(String recipeName) { this.recipeName = recipeName; }
    public void setDescription(String description) { this.description = description; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public void setIngredients(Map<String, String> ingredients) { this.ingredients = ingredients; }
    public void setIngredientDetails(Map<String, IngredientDetail> ingredientDetails) {
        this.ingredientDetails = ingredientDetails;
    }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setHealthScore(double healthScore) { this.healthScore = healthScore; }
    public void setPreparationTime(int preparationTime) { this.preparationTime = preparationTime; }
    public void setCookingTime(int cookingTime) { this.cookingTime = cookingTime; }
    public void setServings(int servings) { this.servings = servings; }
    public void setCategory(String category) { this.category = category; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCreatedDate(long createdDate) { this.createdDate = createdDate; }
    public void setUpdatedDate(long updatedDate) { this.updatedDate = updatedDate; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public void setCookCount(int cookCount) { this.cookCount = cookCount; }
    public void setApproved(boolean approved) { isApproved = approved; }
    public void setFeatured(boolean featured) { isFeatured = featured; }
    public void setProtein(double protein) { this.protein = protein; }
    public void setCarbs(double carbs) { this.carbs = carbs; }
    public void setFat(double fat) { this.fat = fat; }
    public void setFiber(double fiber) { this.fiber = fiber; }
    public void setSugar(double sugar) { this.sugar = sugar; }
    public void setSodium(double sodium) { this.sodium = sodium; }
    public void setTags(Map<String, String> tags) { this.tags = tags; }

    @Exclude
    public int getTotalTime() {
        return preparationTime + cookingTime;
    }

    @Exclude
    public List<String> getIngredientsList() {
        return new ArrayList<>(ingredients.values());
    }

    @Exclude
    public List<String> getTagsList() {
        return new ArrayList<>(tags.values());
    }

    // Helper methods to convert List to Map
    @Exclude
    public void setIngredientsFromList(List<String> ingredientList) {
        this.ingredients = new HashMap<>();
        for (int i = 0; i < ingredientList.size(); i++) {
            this.ingredients.put(String.valueOf(i), ingredientList.get(i));
        }
    }

    @Exclude
    public void setTagsFromList(List<String> tagList) {
        this.tags = new HashMap<>();
        for (int i = 0; i < tagList.size(); i++) {
            this.tags.put(String.valueOf(i), tagList.get(i));
        }
    }

    @IgnoreExtraProperties
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
}