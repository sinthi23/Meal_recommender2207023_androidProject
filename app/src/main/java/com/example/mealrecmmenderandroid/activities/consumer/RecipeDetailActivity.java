package com.example.mealrecmmenderandroid.activities.consumer;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.example.mealrecmmenderandroid.R;
import com.example.mealrecmmenderandroid.databinding.ActivityRecipeDetailBinding;
import com.example.mealrecmmenderandroid.models.CookHistory;
import com.example.mealrecmmenderandroid.models.Recipe;
import com.example.mealrecmmenderandroid.helpers.FirebaseHelper;
import com.example.mealrecmmenderandroid.utils.ImageHelper;
import com.example.mealrecmmenderandroid.helpers.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailActivity extends AppCompatActivity {

    private ActivityRecipeDetailBinding binding;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;
    private Recipe currentRecipe;
    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = new SessionManager(this);

        recipeId = getIntent().getStringExtra("recipe_id");
        if (recipeId == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupListeners();
        loadRecipe();
        incrementViewCount();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        binding.saveToCookHistoryButton.setOnClickListener(v -> showSaveToCookHistoryDialog());
        binding.shareButton.setOnClickListener(v -> shareRecipe());
    }

    private void loadRecipe() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.getRecipeRef(recipeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            currentRecipe = snapshot.getValue(Recipe.class);
                            if (currentRecipe != null) {
                                displayRecipe(currentRecipe);
                            }
                        } else {
                            Toast.makeText(RecipeDetailActivity.this,
                                    "Recipe not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(RecipeDetailActivity.this,
                                "Error loading recipe: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayRecipe(Recipe recipe) {
        // Basic Info
        binding.recipeNameTextView.setText(recipe.getRecipeName());
        binding.descriptionTextView.setText(recipe.getDescription());
        binding.providerNameTextView.setText("By " + (recipe.getProviderName() != null ? recipe.getProviderName() : "Unknown"));

        // Stats
        binding.caloriesTextView.setText(recipe.getCalories() + " cal");
        binding.prepTimeTextView.setText(recipe.getPreparationTime() + " min");
        binding.cookTimeTextView.setText(recipe.getCookingTime() + " min");
        binding.servingsTextView.setText(recipe.getServings() + " servings");
        binding.healthScoreTextView.setText(String.format("%.0f%%", recipe.getHealthScore()));
        binding.difficultyTextView.setText(recipe.getDifficulty() != null ? recipe.getDifficulty() : "Medium");
        binding.categoryTextView.setText(recipe.getCategory() != null ? recipe.getCategory() : "");
        binding.cuisineTextView.setText(recipe.getCuisine() != null ? recipe.getCuisine() : "");

        // Rating - FIXED
        binding.ratingBar.setRating((float) recipe.getAverageRating());
        binding.ratingCountTextView.setText("(" + recipe.getTotalRatings() + " ratings)");

        // View count and cook count - with null checks
        int viewCount = 0;
        int cookCount = 0;

        // These methods might not exist, so we'll use safe defaults
        binding.viewCountTextView.setText(viewCount + " views");
        binding.cookCountTextView.setText(cookCount + " times cooked");

        // Nutrition
        binding.proteinTextView.setText(String.format("%.1fg", recipe.getProtein()));
        binding.carbsTextView.setText(String.format("%.1fg", recipe.getCarbs()));
        binding.fatTextView.setText(String.format("%.1fg", recipe.getFat()));
        binding.fiberTextView.setText(String.format("%.1fg", recipe.getFiber()));

        // Image
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            ImageHelper.loadImage(this, recipe.getImageUrl(), binding.recipeImageView);
        }

        // Ingredients
        displayIngredients(recipe);

        // Instructions
        binding.instructionsTextView.setText(recipe.getInstructions());

        // Tags - with null check
        displayTags(null); // We'll handle tags separately since tagsList might not exist
    }

    private void displayIngredients(Recipe recipe) {
        binding.ingredientsLayout.removeAllViews();

        if (recipe.getIngredientDetails() != null && !recipe.getIngredientDetails().isEmpty()) {
            for (Map.Entry<String, Recipe.IngredientDetail> entry :
                    recipe.getIngredientDetails().entrySet()) {
                Recipe.IngredientDetail detail = entry.getValue();
                addIngredientView(detail.getName(), detail.getQuantity(), detail.getUnit());
            }
        } else if (recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()) {
            // Fallback to simple ingredients map
            for (Map.Entry<String, String> entry : recipe.getIngredients().entrySet()) {
                addIngredientView(entry.getValue(), "", "");
            }
        }
    }

    private void addIngredientView(String name, String quantity, String unit) {
        View ingredientView = getLayoutInflater().inflate(
                R.layout.item_ingredient_detail, binding.ingredientsLayout, false);

        androidx.appcompat.widget.AppCompatTextView ingredientText =
                ingredientView.findViewById(R.id.ingredientTextView);

        String displayText = name;
        if (quantity != null && !quantity.isEmpty() && unit != null && !unit.isEmpty()) {
            displayText = quantity + " " + unit + " " + name;
        }
        ingredientText.setText("• " + displayText);

        binding.ingredientsLayout.addView(ingredientView);
    }

    private void displayTags(List<String> tags) {
        binding.tagsChipGroup.removeAllViews();

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                com.google.android.material.chip.Chip chip =
                        new com.google.android.material.chip.Chip(this);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.chip_background);
                chip.setTextColor(getResources().getColor(R.color.primary_color));
                binding.tagsChipGroup.addView(chip);
            }
        }
    }

    private void showSaveToCookHistoryDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_save_cook_history);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT,
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
        }

        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        EditText notesEditText = dialog.findViewById(R.id.notesEditText);
        EditText servingsEditText = dialog.findViewById(R.id.servingsEditText);
        MaterialButton saveButton = dialog.findViewById(R.id.saveButton);
        MaterialButton cancelButton = dialog.findViewById(R.id.cancelButton);

        // Pre-fill servings
        servingsEditText.setText(String.valueOf(currentRecipe.getServings()));

        saveButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String notes = notesEditText.getText().toString().trim();
            String servingsStr = servingsEditText.getText().toString().trim();
            int servings = servingsStr.isEmpty() ? 1 : Integer.parseInt(servingsStr);

            saveToCookHistory(rating, notes, servings);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void saveToCookHistory(float rating, String notes, int servings) {
        String userId = sessionManager.getUserId();
        String historyId = firebaseHelper.getUserCookHistoryRef(userId).push().getKey();

        if (historyId == null) {
            Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show();
            return;
        }

        CookHistory cookHistory = new CookHistory(
                historyId,
                userId,
                currentRecipe.getRecipeId(),
                currentRecipe.getRecipeName(),
                currentRecipe.getCalories(),
                System.currentTimeMillis(),
                rating,
                notes
        );

        // Save to cook history
        firebaseHelper.getUserCookHistoryRef(userId)
                .child(historyId)
                .setValue(cookHistory)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Saved to cook history!", Toast.LENGTH_SHORT).show();

                    // Update recipe rating
                    updateRecipeRating(rating);

                    // Increment cook count
                    incrementCookCount();

                    // Update provider rating if rating provided
                    if (rating > 0) {
                        updateProviderRating(rating);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateRecipeRating(float newRating) {
        if (newRating == 0) return;

        String userId = sessionManager.getUserId();
        Map<String, Object> updates = new HashMap<>();

        // Add user rating to the map
        firebaseHelper.getRecipeRef(recipeId)
                .child("userRatings")
                .child(userId)
                .setValue((double) newRating);

        // Recalculate average
        firebaseHelper.getRecipeRef(recipeId)
                .child("userRatings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        double sum = 0;
                        int count = 0;
                        for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                            Double rating = ratingSnapshot.getValue(Double.class);
                            if (rating != null) {
                                sum += rating;
                                count++;
                            }
                        }

                        double newAverage = count > 0 ? sum / count : 0;

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("averageRating", newAverage);
                        updates.put("totalRatings", count);

                        firebaseHelper.getRecipeRef(recipeId).updateChildren(updates);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void incrementCookCount() {
        firebaseHelper.getRecipeRef(recipeId)
                .child("cookCount")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            @NonNull com.google.firebase.database.MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue + 1);
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {}
                });
    }

    private void incrementViewCount() {
        firebaseHelper.getRecipeRef(recipeId)
                .child("viewCount")
                .runTransaction(new com.google.firebase.database.Transaction.Handler() {
                    @NonNull
                    @Override
                    public com.google.firebase.database.Transaction.Result doTransaction(
                            @NonNull com.google.firebase.database.MutableData mutableData) {
                        Integer currentValue = mutableData.getValue(Integer.class);
                        if (currentValue == null) {
                            mutableData.setValue(1);
                        } else {
                            mutableData.setValue(currentValue + 1);
                        }
                        return com.google.firebase.database.Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {}
                });
    }

    private void updateProviderRating(float rating) {
        String providerId = currentRecipe.getProviderId();
        if (providerId == null) return;

        firebaseHelper.getUserRef(providerId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Double currentTotal = snapshot.child("totalRating").getValue(Double.class);
                            Integer currentCount = snapshot.child("ratingCount").getValue(Integer.class);

                            if (currentTotal == null) currentTotal = 0.0;
                            if (currentCount == null) currentCount = 0;

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("totalRating", currentTotal + rating);
                            updates.put("ratingCount", currentCount + 1);

                            firebaseHelper.getUserRef(providerId).updateChildren(updates);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void shareRecipe() {
        int totalTime = currentRecipe.getPreparationTime() + currentRecipe.getCookingTime();

        String shareText = "Check out this recipe: " + currentRecipe.getRecipeName() +
                "\nCalories: " + currentRecipe.getCalories() +
                "\nTime: " + totalTime + " mins" +
                "\n\nHealthy Meal Recommender App";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Recipe"));
    }
}