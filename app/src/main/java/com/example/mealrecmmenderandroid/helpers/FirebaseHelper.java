package com.example.mealrecmmenderandroid.helpers;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private FirebaseHelper() {
        auth = FirebaseAuth.getInstance();

        // IMPORTANT: Use explicit database URL
        database = FirebaseDatabase.getInstance("https://meal-recommender-android-9801b-default-rtdb.firebaseio.com");

        storage = FirebaseStorage.getInstance();

        // Log for debugging
        Log.d("FirebaseHelper", "Database URL: " + database.getReference().toString());

        // Enable offline persistence
        try {
            database.setPersistenceEnabled(true);
        } catch (Exception e) {
            Log.d("FirebaseHelper", "Persistence already enabled");
        }
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseDatabase getDatabase() {
        return database;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    // Database References
    public DatabaseReference getUsersRef() {
        return database.getReference("users");
    }

    public DatabaseReference getUserRef(String userId) {
        return database.getReference("users").child(userId);
    }

    public DatabaseReference getRecipesRef() {
        return database.getReference("recipes");
    }

    public DatabaseReference getRecipeRef(String recipeId) {
        return database.getReference("recipes").child(recipeId);
    }

    public DatabaseReference getCookHistoryRef() {
        return database.getReference("cookHistory");
    }

    public DatabaseReference getUserCookHistoryRef(String userId) {
        return database.getReference("cookHistory").child(userId);
    }

    public DatabaseReference getAwardsRef() {
        return database.getReference("awards");
    }

    public DatabaseReference getYearAwardsRef(int year) {
        return database.getReference("awards").child(String.valueOf(year));
    }

    public DatabaseReference getIngredientsRef() {
        return database.getReference("ingredients");
    }

    // Comments References - NEW
    public DatabaseReference getRecipeCommentsRef(String recipeId) {
        return database.getReference("recipe_comments").child(recipeId);
    }

    public DatabaseReference getAllCommentsRef() {
        return database.getReference("recipe_comments");
    }

    // Storage References
    public StorageReference getRecipeImagesRef() {
        return storage.getReference("recipe_images");
    }

    public StorageReference getProfileImagesRef() {
        return storage.getReference("profile_images");
    }

    // User helpers
    public String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public String getCurrentUserEmail() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
    }
}