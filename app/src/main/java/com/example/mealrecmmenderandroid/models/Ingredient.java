package com.example.mealrecmmenderandroid.models;

public class Ingredient {
    private String name;
    private String category;

    // Default constructor required for Firebase
    public Ingredient() {
    }

    public Ingredient(String name, String category) {
        this.name = name;
        this.category = category;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}