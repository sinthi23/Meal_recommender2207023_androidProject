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
    private Object userRatings; // Changed to Object to handle both Map and List

    // Changed to Object to handle both Map and List from Firebase
    private Object ingredients;
    private Object ingredientDetails;

    private boolean approved;
    private long createdAt;

    // Empty constructor required for Firebase
    public Recipe() {
        this.ingredients = new HashMap<String, String>();
        this.ingredientDetails = new HashMap<String, IngredientDetail>();
        this.userRatings = new HashMap<String, Double>();
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

    // Helper methods to safely get data regardless of storage format

    @Exclude
    public Map<String, String> getIngredientsMap() {
        if (ingredients instanceof Map) {
            try {
                return (Map<String, String>) ingredients;
            } catch (ClassCastException e) {
                // If it's Map<String, Boolean> or other type, convert it
                Map<String, ?> rawMap = (Map<String, ?>) ingredients;
                Map<String, String> result = new HashMap<>();
                for (Map.Entry<String, ?> entry : rawMap.entrySet()) {
                    result.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
                return result;
            }
        } else if (ingredients instanceof List) {
            Map<String, String> result = new HashMap<>();
            List<?> list = (List<?>) ingredients;
            for (int i = 0; i < list.size(); i++) {
                result.put(String.valueOf(i), String.valueOf(list.get(i)));
            }
            return result;
        }
        return new HashMap<>();
    }

    @Exclude
    public List<String> getIngredientsList() {
        if (ingredients instanceof List) {
            List<?> rawList = (List<?>) ingredients;
            List<String> result = new ArrayList<>();
            for (Object item : rawList) {
                result.add(String.valueOf(item));
            }
            return result;
        } else if (ingredients instanceof Map) {
            Map<String, ?> map = (Map<String, ?>) ingredients;
            return new ArrayList<>(map.keySet());
        }
        return new ArrayList<>();
    }

    @Exclude
    public Map<String, IngredientDetail> getIngredientDetailsMap() {
        if (ingredientDetails instanceof Map) {
            try {
                return (Map<String, IngredientDetail>) ingredientDetails;
            } catch (ClassCastException e) {
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    @Exclude
    public Map<String, Double> getUserRatingsMap() {
        if (userRatings instanceof Map) {
            try {
                return (Map<String, Double>) userRatings;
            } catch (ClassCastException e) {
                return new HashMap<>();
            }
        }
        return new HashMap<>();
    }

    // Helper method to convert list to map
    @Exclude
    public void setIngredientsFromList(List<String> ingredientsList) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < ingredientsList.size(); i++) {
            map.put(String.valueOf(i), ingredientsList.get(i));
        }
        this.ingredients = map;
    }

    // Helper method to add a rating
    @Exclude
    public void addRating(String userId, double rating) {
        Map<String, Double> ratingsMap = getUserRatingsMap();
        ratingsMap.put(userId, rating);
        this.userRatings = ratingsMap;
        calculateAverageRating();
    }

    // Calculate average rating from all user ratings
    @Exclude
    private void calculateAverageRating() {
        Map<String, Double> ratingsMap = getUserRatingsMap();
        if (ratingsMap.isEmpty()) {
            averageRating = 0.0;
            totalRatings = 0;
            return;
        }

        double sum = 0;
        for (Double rating : ratingsMap.values()) {
            sum += rating;
        }
        totalRatings = ratingsMap.size();
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

    // Return Object for Firebase serialization
    public Object getUserRatings() { return userRatings; }
    public void setUserRatings(Object userRatings) { this.userRatings = userRatings; }

    public Object getIngredients() { return ingredients; }
    public void setIngredients(Object ingredients) { this.ingredients = ingredients; }

    public Object getIngredientDetails() { return ingredientDetails; }
    public void setIngredientDetails(Object ingredientDetails) {
        this.ingredientDetails = ingredientDetails;
    }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}